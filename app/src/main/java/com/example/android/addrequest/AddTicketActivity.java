package com.example.android.addrequest;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.android.addrequest.database.AppDatabase;
import com.example.android.addrequest.database.TicketEntry;

import java.util.Date;

public class AddTicketActivity extends AppCompatActivity{


    /**
     * Initialize values.
     */

    // Extra for the ticket ID to be received in the intent
    public static final String EXTRA_TICKET_ID = "extraTicketId";
    // Extra for the ticket ID to be received after rotation
    public static final String INSTANCE_TICKET_ID = "instanceTicketId";
    // Constant for default ticket id to be used when not in update mode
    private static final int DEFAULT_TICKET_ID = -1;

    // Initialize integer for ticket ID
    private int mTicketId = DEFAULT_TICKET_ID;

    // Member variable for the Database
    private AppDatabase mDb;

    // Fields for views
    Button mButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TICKET_ID)) {
            mTicketId = savedInstanceState.getInt(INSTANCE_TICKET_ID, DEFAULT_TICKET_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TICKET_ID)) {
            mButton.setText(R.string.update_button);
            if (mTicketId == DEFAULT_TICKET_ID) {

                // populate the UI
                mTicketId = intent.getIntExtra(EXTRA_TICKET_ID, DEFAULT_TICKET_ID);

                // Declare a AddTicketViewModelFactory using mDb and mTicketId
                AddTicketViewModelFactory factory = new AddTicketViewModelFactory(mDb, mTicketId);

                // Declare a AddTicketViewModel variable and initialize it by calling ViewModelProviders.of
                // for that use the factory created above AddTicketViewModel
                final AddTicketViewModel viewModel
                        = ViewModelProviders.of(this, factory).get(AddTicketViewModel.class);

                // Observe the LiveData object in the ViewModel. Use it also when removing the observer
                viewModel.getTicket().observe(this, new Observer<TicketEntry>() {
                    @Override
                    public void onChanged(@Nullable TicketEntry ticketEntry) {
                        viewModel.getTicket().removeObserver(this);
                        populateUI(ticketEntry);
                    }
                });
            }
        }
                
    }


    /**
     * Save the instance ticket ID in case of screen rotation or app exit
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TICKET_ID, mTicketId);
        super.onSaveInstanceState(outState);
    }


    /**
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }


    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param task the ticketEntry to populate the UI
     */
    private void populateUI(TicketEntry task) {
        if (task == null) {
            return;
        }
    }

    
    /**
     * Add or update entry when SAVE button is clicked.
     */
    public void onSaveButtonClicked() {

        // Set up ticket
        String title = "test title";
        String description = "test description";
        Date date = new Date();
        final TicketEntry ticket = new TicketEntry(title, description, date);

        // Execute ticket entry
        AppExecuters.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mTicketId == DEFAULT_TICKET_ID) {
                    // insert new task
                    mDb.ticketDao().insertTicket(ticket);
                } else {
                    //update task
                    ticket.setId(mTicketId);
                    mDb.ticketDao().updateTicket(ticket);
                }
                finish();
            }
        });

    }


}
