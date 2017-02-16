/*
 * Copyright (c) 2015 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.divya.todolist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getName();
  private final int ADD_TASK_REQUEST = 1;

  private final String PREFS_TASKS = "prefs_tasks";
  private final String KEY_TASKS_LIST = "list";

  private ArrayList<String> mList;
  private ArrayAdapter<String> mAdapter;
  private TextView mDateTimeTextView;
  private BroadcastReceiver mTickReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // 1 You call onCreate() on the superclass;
    // remember that this is always the first thing you should do in a callback method
    super.onCreate(savedInstanceState);

    // 2 -Make the activity full screen
    //You tell the WindowManager to make your activity’s window full screen.
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // 3 You set the content view of your activity with the corresponding layout file resource.
    setContentView(R.layout.activity_main);

    // 4 Here you initialize all the UI and data variables.
    // In this case, you’re using a TextView to show the current date and time,
    // a button to add tasks to your list,
    // a ListView to display your list,
    // and an ArrayList to hold your data.
    // You can find the implementation of all these UI elements in the activity_main.xml file.
    mDateTimeTextView = (TextView) findViewById(R.id.dateTimeTextView);
    final Button addTaskBtn = (Button) findViewById(R.id.addTaskBtn);
    final ListView listview = (ListView) findViewById(R.id.taskListview);
    mList = new ArrayList<String>();

    String savedList = getSharedPreferences(PREFS_TASKS, MODE_PRIVATE).getString(KEY_TASKS_LIST, null);
    if (savedList != null) {
      String[] items = savedList.split(",");
      mList = new ArrayList<String>(Arrays.asList(items));
    }


    // 5 Here you initialize and set the Adapter that will handle the data for your ListView
    mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);
    listview.setAdapter(mAdapter);

    // 6 You set an OnItemClickListener() for the ListView to capture the user’s tap on individual list entries.

    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        taskSelected(i);
      }
    });

    //initialize the BroadcastReceiver
    mTickReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
          mDateTimeTextView.setText(getCurrentTimeStamp());
        }
      }
    };
  }

  @Override
  protected void onResume() {
    // 1 You call onResume() on the superclass.
    super.onResume();
    // 2 You set the date and time TextView with the current value.
    mDateTimeTextView.setText(getCurrentTimeStamp());
    // 3 You register the broadcast receiver in onResume(). This ensures it will receive the broadcasts for ACTION_TIME_TICK.
    // These are sent every minute after the time changes.
    registerReceiver(mTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
  }

  @Override
  protected void onPause() {
    // 4 You call onPause() on the superclass.
    super.onPause();
    // 5 You unregister the broadcast receiver in onPause(),
    // so the app no longer receives the time change broadcasts.
    // This cuts down unnecessary system overhead.
    if (mTickReceiver != null) {
      try {
        unregisterReceiver(mTickReceiver);
      } catch (IllegalArgumentException e) {
        Log.e(TAG, "Timetick Receiver not registered", e);
      }
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Save all data which you want to persist.
    StringBuilder savedList = new StringBuilder();
    for (String s : mList) {
      savedList.append(s);
      savedList.append(",");
    }
    getSharedPreferences(PREFS_TASKS, MODE_PRIVATE).edit()
            .putString(KEY_TASKS_LIST, savedList.toString()).commit();
  }

  public void addTaskClicked(View view) {
    Intent intent = new Intent(MainActivity.this, TaskDescriptionActivity.class);
    startActivityForResult(intent, ADD_TASK_REQUEST);

  }

  private static String getCurrentTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");//dd/MM/yyyy
    Date now = new Date();
    String strDate = sdf.format(now);
    return strDate;
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // 1 You check the requestCode to ensure the returned result is indeed for your add task request
    // you started with TaskDescriptionActivity.

    if (requestCode == ADD_TASK_REQUEST) {
      // 2 You make sure the resultCode is RESULT_OK —
      // the standard activity result for a successful operation.
      if (resultCode == RESULT_OK) {
        // 3 Here you extract the task description from the result intent and add it to your list.
        String task = data.getStringExtra(TaskDescriptionActivity.EXTRA_TASK_DESCRIPTION);
        mList.add(task);
        // 4 Finally, you call notifyDataSetChanged() on your list adapter.
        // In turn, it notifies the listView about changes in your data model so it can trigger a refresh of its view.
        mAdapter.notifyDataSetChanged();
      }
    }
  }
  private void taskSelected(final int position) {
    // 1 You create an AlertDialog.Builder which facilitates the creation of an AlertDialog.
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

    // 2 You set the alert dialog title.
    alertDialogBuilder.setTitle(R.string.alert_title);

    // 3 You set the alert dialog message to be the description of the selected task.
    // Then you also implement the PositiveButton to remove the item from the list and refresh it,
    // and the NegativeButton to dismiss the dialog.
    alertDialogBuilder
            .setMessage(mList.get(position))
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                mList.remove(position);
                mAdapter.notifyDataSetChanged();
              }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
              }
            });

    // 4 You create the alert dialog from the AlertDialog.Builder object.
    AlertDialog alertDialog = alertDialogBuilder.create();

    // 5 You display the alert dialog to the user.
    alertDialog.show();
  }
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }
}
