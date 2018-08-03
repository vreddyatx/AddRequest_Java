package project.files.android.addrequest.MVVM.AddTicket;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import project.files.android.addrequest.Database.AppDatabase;
import project.files.android.addrequest.Database.AppExecuters;
import project.files.android.addrequest.Database.TicketEntry;
import project.files.android.addrequest.Notification.Notifications;
import project.files.android.addrequest.Utils.GlobalConstants;

public class AddTicketViewModel extends ViewModel {


    /**
     * Initialize variables.
     */

    // Constant for logging
    private static final String TAG = AddTicketViewModel.class.getSimpleName();

    // RequestsDO member variable for the TicketEntry object wrapped in a LiveData.
    private LiveData<TicketEntry> ticket;

    // Initialize database
    private AppDatabase database;


    /**
     * Constructor where you call loadTicketById of the ticketDao to initialize the tickets variable.
     * Note: The constructor receives the database and the ticketId
     */
    /*
    public AddTicketViewModel(Application application, int ticketId) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
        loadTicket(ticketId);

    }
    */
    public void setup(Context context, int ticketId){
        database = AppDatabase.getInstance(context);
        loadTicket(ticketId);
    }



    /**
     * Load ticket.
     */
    private void loadTicket(int ticketId){
        ticket = database.ticketDao().loadTicketById(ticketId);
    }


    /**
     * Add ticket.
     */
    public void addTicket(
            final int ticketId,
            final String ticketTitle,
            final String ticketDescription,
            final String ticketDate,
            final String ticketVideoPostId,
            final String ticketVideoLocalUri,
            final String ticketVideoInternetUrl,
            final String userId,
            final String userName,
            final String userPhotoUrl,
            final int ticketType){

        Log.d(TAG,"ticketVideoPostId:  " + ticketVideoPostId);

        //final Context context = this.getApplication();

        if (ticketVideoPostId.equals(GlobalConstants.VIDEO_CREATED_TICKET_VIDEO_POST_ID)){

            Uri capturedVideoUri = Uri.parse(ticketVideoLocalUri);

            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference firebaseVideoRef = firebaseStorage.getReference().child("Videos");
            StorageReference localVideoRef = firebaseVideoRef.child(capturedVideoUri.getLastPathSegment());
            UploadTask uploadTask = localVideoRef.putFile(capturedVideoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    Log.d(TAG,"Exception:  " + exception);

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String ticketVideoPostId = GlobalConstants.VIDEO_EXISTS_TICKET_VIDEO_POST_ID;
                    String ticketVideoInternetUrl;

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();;
                    Log.d(TAG,"download URL:  " + downloadUrl);
                    ticketVideoInternetUrl = downloadUrl.toString();

                    FirebaseDatabase fBdatabase = FirebaseDatabase.getInstance();
                    final DatabaseReference myRef = fBdatabase.getReference("Tickets");

                    final TicketEntry ticketEntry = new TicketEntry(
                            ticketId,
                            ticketTitle,
                            ticketDescription,
                            ticketDate,
                            ticketVideoPostId,
                            ticketVideoLocalUri,
                            ticketVideoInternetUrl,
                            userId,
                            userName,
                            userPhotoUrl);

                    final FirebaseDbTicket fBticket = new FirebaseDbTicket(
                            ticketId,
                            ticketTitle,
                            ticketDescription,
                            ticketDate,
                            ticketVideoPostId,
                            ticketVideoLocalUri,
                            ticketVideoInternetUrl,
                            userId,
                            userName,
                            userPhotoUrl);


                    if(ticketType == GlobalConstants.ADD_TICKET_TYPE){
                        AppExecuters.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                database.ticketDao().insertTicket(ticketEntry);
                            }
                        });
                    } else {
                        AppExecuters.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                database.ticketDao().updateTicket(ticketEntry);
                            }
                        });
                    }

                    AppExecuters.getInstance().networkIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            myRef.child(String.valueOf(ticketId)).setValue(fBticket);
                        }
                    });

                    //Notifications.ticketPostedNotification(context,ticketId);

                }
            });

        } else {

            final FirebaseDatabase fBDatabase = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = fBDatabase.getReference("Tickets");

            final TicketEntry ticketEntry = new TicketEntry(
                    ticketId,
                    ticketTitle,
                    ticketDescription,
                    ticketDate,
                    ticketVideoPostId,
                    ticketVideoLocalUri,
                    ticketVideoInternetUrl,
                    userId,
                    userName,
                    userPhotoUrl);

            final FirebaseDbTicket fBTicket = new FirebaseDbTicket(
                    ticketId,
                    ticketTitle,
                    ticketDescription,
                    ticketDate,
                    ticketVideoPostId,
                    ticketVideoLocalUri,
                    ticketVideoInternetUrl,
                    userId,
                    userName,
                    userPhotoUrl);

            if(ticketType == GlobalConstants.ADD_TICKET_TYPE){
                AppExecuters.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        database.ticketDao().insertTicket(ticketEntry);
                    }
                });
            } else {
                AppExecuters.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        database.ticketDao().updateTicket(ticketEntry);
                    }
                });
            }

            AppExecuters.getInstance().networkIO().execute(new Runnable() {
                @Override
                public void run() {
                    myRef.child(String.valueOf(ticketId)).setValue(fBTicket);
                }
            });



        }

    }


    /**
    * Getter for the ticket variable.
    */
    public LiveData<TicketEntry> getTicket() {
        return ticket;
    }



}
