package it.reyboz.bustorino.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import it.reyboz.bustorino.ActivityFavorites;
import it.reyboz.bustorino.ActivityMain;
import it.reyboz.bustorino.ActivityMap;
import it.reyboz.bustorino.R;
import it.reyboz.bustorino.adapters.StopAdapter;
import it.reyboz.bustorino.backend.Stop;
import it.reyboz.bustorino.data.FavoritesViewModel;
import it.reyboz.bustorino.data.UserDB;
import it.reyboz.bustorino.middleware.AsyncStopFavoriteAction;

public class FavoritesFragment extends BaseFragment {

    private ListView favoriteListView;
    private EditText busStopNameText;
    private TextView favoriteTipTextView;
    private ImageView angeryBusImageView;

    @Nullable
    private CommonFragmentListener mListener;

    public static final String FRAGMENT_TAG = "BusTOFavFragment";




    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public FavoritesFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //do nothing
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoriteListView = root.findViewById(R.id.favoriteListView);
        favoriteListView.setOnItemClickListener((parent, view, position, id) -> {
            /**
             * Casting because of Javamerda
             * @url http://stackoverflow.com/questions/30549485/androids-list-view-parameterized-type-in-adapterview-onitemclicklistener
             */
            Stop busStop = (Stop) parent.getItemAtPosition(position);

            if(mListener!=null){
                mListener.requestArrivalsForStopID(busStop.ID);
            }

        });
        angeryBusImageView = root.findViewById(R.id.angeryBusImageView);
        favoriteTipTextView = root.findViewById(R.id.favoriteTipTextView);
        registerForContextMenu(favoriteListView);

        FavoritesViewModel model = new ViewModelProvider(this).get(FavoritesViewModel.class);
        model.getFavorites().observe(getViewLifecycleOwner(), this::showStops);

        showStops(new ArrayList<>());
        return root;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CommonFragmentListener) {
            mListener = (CommonFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CommonFragmentListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.favoriteListView) {
            // if we aren't attached to activity, return null
            if (getActivity()==null) return;
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_favourites_entry, menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener!=null) mListener.readyGUIfor(FragmentKind.FAVORITES);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        Stop busStop = (Stop) favoriteListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.action_favourite_entry_delete:
                if (getContext()!=null)
                new AsyncStopFavoriteAction(getContext().getApplicationContext(), AsyncStopFavoriteAction.Action.REMOVE,
                        result -> {

                        }).execute(busStop);

                return true;

            case R.id.action_rename_bus_stop_username:
                showBusStopUsernameInputDialog(busStop);
                return true;
            case R.id.action_view_on_map:
                final String theGeoUrl = busStop.getGeoURL();
                /*
                if(theGeoUrl==null){
                    //doesn't have a position
                    Toast.makeText(getContext(),R.string.cannot_show_on_map_no_position,Toast.LENGTH_SHORT).show();
                    return true;
                }

                // start ActivityMap with these extras in intent
                Intent intent = new Intent(getContext(), ActivityMap.class);
                Bundle b = new Bundle();
                double lat, lon;
                if (busStop.getLatitude()!=null)
                    lat = busStop.getLatitude();
                else lat = 200;
                if (busStop.getLongitude()!=null)
                    lon = busStop.getLongitude();
                else lon = 200;
                b.putDouble("lat", lat);
                b.putDouble("lon",lon);
                b.putString("name", busStop.getStopDefaultName());
                b.putString("ID", busStop.ID);
                intent.putExtras(b);

                startActivity(intent);
                TODO: start map on button press
                 */
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    void showStops(List<Stop> busStops){
        // If no data is found show a friendly message

        if (busStops.size() == 0) {
            favoriteListView.setVisibility(View.INVISIBLE);
           // TextView favoriteTipTextView = (TextView) findViewById(R.id.favoriteTipTextView);
            //assert favoriteTipTextView != null;
            favoriteTipTextView.setVisibility(View.VISIBLE);
            //ImageView angeryBusImageView = (ImageView) findViewById(R.id.angeryBusImageView);
            angeryBusImageView.setVisibility(View.VISIBLE);
        } else {
            favoriteListView.setVisibility(View.VISIBLE);
            favoriteTipTextView.setVisibility(View.INVISIBLE);
            angeryBusImageView.setVisibility(View.INVISIBLE);
        }
        /* There's a nice method called notifyDataSetChanged() to avoid building the ListView
         * all over again. This method exists in a billion answers on Stack Overflow, but
         * it's nowhere to be seen around here, Android Studio can't find it no matter what.
         * Anyway, it only works from Android 2.3 onward (which is why it refuses to appear, I
         * guess) and requires to modify the list with .add() and .clear() and some other
         * methods, so to update a single stop we need to completely rebuild the list for no
         * reason. It would probably end up as "slow" as throwing away the old ListView and
         * redrwaing everything.
         */

        // Show results
        favoriteListView.setAdapter(new StopAdapter(getContext(), busStops));
    }

    public void showBusStopUsernameInputDialog(final Stop busStop) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = this.getLayoutInflater();
        View renameDialogLayout = inflater.inflate(R.layout.rename_dialog, null);

        busStopNameText = (EditText) renameDialogLayout.findViewById(R.id.rename_dialog_bus_stop_name);
        busStopNameText.setText(busStop.getStopDisplayName());
        busStopNameText.setHint(busStop.getStopDefaultName());

        builder.setTitle(getString(R.string.dialog_rename_bus_stop_username_title));
        builder.setView(renameDialogLayout);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String busStopUsername = busStopNameText.getText().toString();
                String oldUserName = busStop.getStopUserName();

                // changed to none
                if(busStopUsername.length() == 0) {
                    // unless it was already empty, set new
                    if(oldUserName != null) {
                        busStop.setStopUserName(null);

                    }
                } else { // changed to something
                    // something different?
                    if(!busStopUsername.equals(oldUserName)) {
                        busStop.setStopUserName(busStopUsername);

                    }
                }
                launchUpdate(busStop);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton(R.string.dialog_rename_bus_stop_username_reset_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // delete user name from database
                busStop.setStopUserName(null);
                launchUpdate(busStop);

            }
        });
        builder.show();
    }

    private void launchUpdate(Stop busStop){
        if (getContext()!=null)
            new AsyncStopFavoriteAction(getContext().getApplicationContext(), AsyncStopFavoriteAction.Action.UPDATE,
                    new AsyncStopFavoriteAction.ResultListener() {
                        @Override
                        public void doStuffWithResult(Boolean result) {
                            //Toast.makeText(getApplicationContext(), R.string.tip_add_favorite, Toast.LENGTH_SHORT).show();
                        }
                    }).execute(busStop);
    }
}