/*
 * Copyright 2013 Andrew Okin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.forkk.autocron;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.forkk.autocron.data.NfcService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Activity for writing NFC rule data to an NFC tag.
 */
public class NfcRuleWriteActivity extends Activity implements DialogInterface.OnCancelListener
{
    public static final String EXTRA_NFC_RULE_ID = "net.forkk.autocron.nfc_rule_id";

    public static final String EXTRA_NFC_ACTION = "net.forkk.autocron.nfc_rule_action_type";

    protected ProgressDialog mProgressDialog;

    protected WriteTagTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Show a dialog waiting for the user to tap the tag.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setTitle(getString(R.string.nfc_write_wait_title));
        mProgressDialog.setMessage(getString(R.string.nfc_write_wait_message));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onStart();

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        assert adapter != null;

        mProgressDialog.show();

        String id = getIntent().getStringExtra(EXTRA_NFC_RULE_ID);
        String action = getIntent().getStringExtra(EXTRA_NFC_ACTION);

        Intent intent = new Intent(this, NfcRuleWriteActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_NFC_RULE_ID, id);
        intent.putExtra(EXTRA_NFC_ACTION, action);
        PendingIntent pending =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        IntentFilter detectionFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        adapter.enableForegroundDispatch(this, pending, new IntentFilter[] {detectionFilter}, null);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mProgressDialog.cancel();
        mProgressDialog.hide();
        if (mTask != null) mTask.cancel(true);

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        assert adapter != null;
        adapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mProgressDialog.dismiss();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        mProgressDialog.setMessage(getString(R.string.nfc_writing_message));

        mTask = new WriteTagTask();
        mTask.execute(intent);
    }

    private class WriteTagTask extends AsyncTask<Intent, Void, WriteTagResult>
    {
        @Override
        protected void onPostExecute(WriteTagResult writeTagResult)
        {
            super.onPostExecute(writeTagResult);
            int titleId = writeTagResult.mSuccess ? R.string.title_tag_write_success
                                                  : R.string.error_nfc_tag_generic_title;
            showFinishDialog(getString(titleId), writeTagResult.mMessage);
        }

        @Override
        protected void onCancelled(WriteTagResult writeTagResult)
        {
            super.onCancelled(writeTagResult);

            int titleId = writeTagResult.mSuccess ? R.string.title_tag_write_success
                                                  : R.string.error_nfc_tag_generic_title;
            showFinishDialog(getString(titleId), writeTagResult.mMessage);
        }

        @Override
        protected WriteTagResult doInBackground(Intent... intents)
        {
            Intent intent = intents[0];

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String id = intent.getStringExtra(EXTRA_NFC_RULE_ID);
            String action = intent.getStringExtra(EXTRA_NFC_ACTION);

            assert tag != null;
            assert id != null;
            assert action != null;

            // Neat, IntelliJ made my code look like space ships! Kinda...
            try
            {
                NdefRecord idRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                                                     "application/net.forkk.autocron"
                                                             .getBytes("US-ASCII"), null,
                                                     id.getBytes());
                NdefRecord actionRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                                                         "application/net.forkk.autocron"
                                                                 .getBytes("US-ASCII"), null,
                                                         action.getBytes());

                NdefMessage message = new NdefMessage(new NdefRecord[] {idRecord, actionRecord});

                if (isCancelled())
                    return new WriteTagResult(false, R.string.error_tag_write_cancelled);

                // If the tag is already NDEF formatted, write data to it, otherwise, 
                // try to format it to NDEF.
                Ndef ndef = Ndef.get(tag);
                if (ndef != null) return writeNdef(ndef, message);
                else
                {
                    NdefFormatable formatable = NdefFormatable.get(tag);
                    if (formatable != null) return formatNdef(formatable, message);
                    else return new WriteTagResult(false, R.string.error_message_tag_cant_ndef);
                }
            }
            catch (UnsupportedEncodingException e)
            {
                Log.wtf(NfcService.LOGGER_TAG, "Unsupported encoding. Wat?", e);
                return new WriteTagResult(false, R.string.error_message_unexpected);
            }
        }

        /**
         * Write data to already formatted tag.
         */
        protected WriteTagResult writeNdef(Ndef ndef, NdefMessage message)
        {
            try
            {
                ndef.connect();

                if (isCancelled())
                    return new WriteTagResult(false, R.string.error_tag_write_cancelled);

                if (!ndef.isWritable())
                    return new WriteTagResult(false, R.string.error_message_tag_read_only);

                if (ndef.getMaxSize() < message.toByteArray().length)
                    return new WriteTagResult(false, R.string.error_message_tag_too_small);

                try
                {
                    ndef.writeNdefMessage(message);

                    return new WriteTagResult(true, R.string.message_tag_write_success);
                }
                catch (TagLostException e)
                {
                    return new WriteTagResult(false, R.string.error_message_tag_lost);
                }
                catch (IOException e)
                {
                    return new WriteTagResult(false, R.string.error_message_tag_write_unknown);
                }
                catch (FormatException e)
                {
                    return new WriteTagResult(false, R.string.error_message_malformed_ndef_message);
                }
            }
            catch (IOException e)
            {
                return new WriteTagResult(false, R.string.error_message_tag_connect_error);
            }
        }

        /**
         * Format a tag to NDEF.
         */
        protected WriteTagResult formatNdef(NdefFormatable formatable, NdefMessage message)
        {
            try
            {
                formatable.connect();

                if (isCancelled())
                    return new WriteTagResult(false, R.string.error_tag_write_cancelled);

                formatable.format(message);

                return new WriteTagResult(true, R.string.message_tag_write_success);
            }
            catch (TagLostException e)
            {
                return new WriteTagResult(false, R.string.error_message_tag_lost);
            }
            catch (IOException e)
            {
                return new WriteTagResult(false, R.string.error_message_tag_formatting_error);
            }
            catch (FormatException e)
            {
                return new WriteTagResult(false, R.string.error_message_tag_formatting_error);
            }
        }
    }

    private class WriteTagResult
    {
        public boolean mSuccess;

        public String mMessage;

        public WriteTagResult(boolean success, int messageId)
        {
            mSuccess = success;
            mMessage = getString(messageId);
        }

        public WriteTagResult(boolean success, int messageId, Object... formatArgs)
        {
            mSuccess = success;
            mMessage = getString(messageId, formatArgs);
        }
    }

    public void showFinishDialog(String title, String message)
    {
        mProgressDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton(R.string.okay, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface)
    {
        finish();
    }
}
