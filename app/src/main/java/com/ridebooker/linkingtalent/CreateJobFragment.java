package com.ridebooker.linkingtalent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ridebooker.linkingtalent.datatypes.Job;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateJobFragment extends Fragment
{

    Button btnButton;
    EditText etCompany, etTitle, etFirstCat, etSecondcat, etBounty, etLocation, etDescription;
    SeekBar bountySeekBar;
    Spinner minSpinner, maxSpinner;
    TextView tvBounty;

    public CreateJobFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_job, container, false);

        btnButton = (Button) view.findViewById(R.id.create_job_insert_button);
        etCompany = (EditText) view.findViewById(R.id.create_job_company_name);
        etTitle = (EditText) view.findViewById(R.id.create_job_title);
        etFirstCat = (EditText) view.findViewById(R.id.create_job_first_cat);
        etSecondcat = (EditText) view.findViewById(R.id.create_job_second_cat);
        etLocation = (EditText) view.findViewById(R.id.create_job_location);
        etDescription = (EditText) view.findViewById(R.id.create_job_description);
        bountySeekBar = (SeekBar) view.findViewById(R.id.create_job_seekBar);
        minSpinner = (Spinner) view.findViewById(R.id.create_job_min_spinner);
        maxSpinner = (Spinner) view.findViewById(R.id.create_job_max_spinner);
        tvBounty = (TextView) view.findViewById(R.id.create_job_tv_bounty_value);

        initGui();

        // Inflate the layout for this fragment
        return view;
    }

    private void initGui()
    {
        bountySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                tvBounty.setText("$" + Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        btnButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkFields())
                {
                    int max, min;
                    min = Integer.parseInt(minSpinner.getSelectedItem().toString());
                    max = Integer.parseInt(maxSpinner.getSelectedItem().toString());

                    Job j = new Job(etTitle.getText().toString(), etCompany.getText().toString());

                    j.setFirstCategory(etFirstCat.getText().toString());
                    j.setSecondCategory(etSecondcat.getText().toString());
                    j.setPayMin(min);
                    j.setPayMax(max);
                    j.setBounty(bountySeekBar.getProgress());
                    j.setDescription(etDescription.getText().toString());
                    j.setLocation(etLocation.getText().toString());
                    MainActivity.dbJobRef.push().setValue(j);
                    Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(), "Field empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkFields()
    {
        if (etDescription.getText().toString().matches("") ||
                etLocation.getText().toString().matches("") ||
                etSecondcat.getText().toString().matches("") ||
                etFirstCat.getText().toString().matches("") ||
                etTitle.getText().toString().matches("") ||
                etCompany.getText().toString().matches("") ||
                tvBounty.getText().toString().matches("")){
            return false;
        }
        return true;

    }

}
