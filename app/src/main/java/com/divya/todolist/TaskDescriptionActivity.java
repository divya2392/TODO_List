package com.divya.todolist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class TaskDescriptionActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_DESCRIPTION = "task";

    private EditText mDescriptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_description);
        mDescriptionView = (EditText) findViewById(R.id.descriptionText);
    }

    public void doneClicked(View view) {
        // 1 You retrieve the task description from the TextView.
        String taskDescription = mDescriptionView.getText().toString();

        if (!taskDescription.isEmpty()) {
            // 2 You create a result intent to pass back to MainActivity
            // if the task description retrieved in step one is not empty.
            // Then you bundle the task description with the intent and set the result to RESULT_OK,
            // indicating that the user successfully entered a task.
            Intent result = new Intent();
            result.putExtra(EXTRA_TASK_DESCRIPTION, taskDescription);
            setResult(RESULT_OK, result);
        } else {
            // 3 You set the result to RESULT_CANCELED,
            // indicating there was no task entered by the user if the task description retrieved in step one is empty.
            setResult(RESULT_CANCELED);
        }
            // 4 Here you close the activity.
        finish();

    }
}
