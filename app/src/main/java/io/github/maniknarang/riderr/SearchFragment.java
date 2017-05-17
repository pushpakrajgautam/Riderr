package io.github.maniknarang.riderr;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener, TextWatcher {
    private EditText autoCompleteTextView;
    private MapActivity mapActivity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.search_frag,container,false);
        view.setClickable(true);
        mapActivity = (MapActivity) getActivity();
        mapActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        autoCompleteTextView = (EditText) view.findViewById(R.id.auto_complete_text_view);
        autoCompleteTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_IMPLICIT_ONLY);
        Typeface font1 = Typeface.createFromAsset(getContext().getAssets(),"Raleway-SemiBold.ttf");
        autoCompleteTextView.setTypeface(font1);
        AutoCompleteAdapter autoCompleteAdapter = new AutoCompleteAdapter(getContext(),R.layout.complete_list_item);
        autoCompleteTextView.addTextChangedListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        autoCompleteTextView.setText(((OptionName)adapterView.getItemAtPosition(i)).getName());
        Toast.makeText(getContext(),"Touched",Toast.LENGTH_LONG);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {
        String query = charSequence.toString();
    }

    @Override
    public void afterTextChanged(Editable editable) {}


}
