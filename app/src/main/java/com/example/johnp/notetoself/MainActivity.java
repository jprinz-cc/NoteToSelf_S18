package com.example.johnp.notetoself;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NoteAdapter mNoteAdapter;
    private boolean mSound;
    private int mAnimOption;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNoteAdapter = new NoteAdapter();

        ListView listNote = (ListView) findViewById(R.id.listView);

        listNote.setAdapter(mNoteAdapter);

        // So we can long click it
        listNote.setLongClickable(true);

        // Now to detect long clicks and delete the note
        listNote.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> adapter, View view,
                                           int whichItem, long id) {

                // Ask NoteAdapter to delete this entry
                mNoteAdapter.deleteNote(whichItem);

                return true;
            }
        });


		// Handle clicks on the ListView
        listNote.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int whichItem, long id) {

				/*
					Create  a temporary Note
					Which is a reference to the Note
					that has just been clicked
				*/
                Note tempNote = mNoteAdapter.getItem(whichItem);

				// Create a new dialog window
                DialogShowNote dialog = new DialogShowNote();

				// Send in a reference to the note to be shown
                dialog.sendNoteSelected(tempNote);

				// Show the dialog window with the note in it
                dialog.show(getFragmentManager(), "");

            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();

        mPrefs = getSharedPreferences("Note to self", MODE_PRIVATE);
        mSound  = mPrefs.getBoolean("sound", true);
        mAnimOption = mPrefs.getInt("sort option", SettingsActivity.FAST);
    }

    public void createNewNote(Note n){

        mNoteAdapter.addNote(n);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_add) {
            DialogNewNote dialog = new DialogNewNote();
            dialog.show(getFragmentManager(), "");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class NoteAdapter extends BaseAdapter {

        private JSONSerializer mSerializer;
        List<Note> noteList = new ArrayList<Note>();

        public NoteAdapter(){

            mSerializer = new JSONSerializer("NoteToSelf.json", MainActivity.this.getApplicationContext());

            try {
                noteList = mSerializer.load();
            } catch (Exception e) {
                noteList = new ArrayList<Note>();
                Log.e("Error loading notes: ", "", e);
            }

        }

        public void saveNotes(){
            try{
                mSerializer.save(noteList);

            }catch(Exception e){
                Log.e("Error Saving Notes","", e);
            }
        }


        @Override
        public int getCount() {
            return noteList.size();
        }

        @Override
        public Note getItem(int whichItem) {
			// Returns the requested note
            return noteList.get(whichItem);
        }

        @Override
        public long getItemId(int whichItem) {
			// Method used internally
            return whichItem;
        }

        @Override
        public View getView(
            int whichItem, View view, ViewGroup viewGroup) {

			/*
				Prepare a list item to show our data
				The list item is contained in the view parameter
				The position of the data in our ArrayList is contained
				in whichItem parameter
			*/


			// Has view been inflated already
            if(view == null){

				// No. So do so here
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                view = inflater.inflate(R.layout.listitem, viewGroup,false);

            }// End if

			// Grab a reference to all our TextView and ImageView widgets
            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            ImageView ivImportant = (ImageView) view.findViewById(R.id.imageViewImportant);
            ImageView ivTodo = (ImageView) view.findViewById(R.id.imageViewTodo);
            ImageView ivIdea = (ImageView) view.findViewById(R.id.imageViewIdea);


			// Hide any ImageView widgets that are not relevant
            Note tempNote = noteList.get(whichItem);


            if (!tempNote.isImportant()){
                ivImportant.setVisibility(View.GONE);
            }

            if (!tempNote.isTodo()){
                ivTodo.setVisibility(View.GONE);
            }

            if (!tempNote.isIdea()){
                ivIdea.setVisibility(View.GONE);
            }

			// Add the text to the heading and description
            txtTitle.setText(tempNote.getTitle());
            txtDescription.setText(tempNote.getDescription());

            return view;
        }

        public void addNote(Note n){

            noteList.add(n);
            notifyDataSetChanged();

        }

        public void deleteNote(int n){

            noteList.remove(n);
            notifyDataSetChanged();


        }



    }

    @Override
    protected void onPause(){
        super.onPause();

        mNoteAdapter.saveNotes();

    }

}
