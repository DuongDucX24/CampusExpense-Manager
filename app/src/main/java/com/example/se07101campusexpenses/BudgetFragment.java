package com.example.se07101campusexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.adapter.BudgetRVAdapter;
import com.example.se07101campusexpenses.budget.AddBudgetActivity;
import com.example.se07101campusexpenses.database.BudgetModel;
import com.example.se07101campusexpenses.database.BudgetRepository;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BudgetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ArrayList<BudgetModel> budgetModelArrayList;
    private BudgetModel budgetModel;

    public BudgetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BudgetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BudgetFragment newInstance(String param1, String param2) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_budget, container, false);
        Button btnCreteBudget = view.findViewById(R.id.btnCreateBudget);
        RecyclerView budgetRv = view.findViewById(R.id.rvBudget);

        budgetModelArrayList = new ArrayList<>();
        BudgetRepository budgetRepository = new BudgetRepository(requireActivity().getApplication());
        budgetModelArrayList = (ArrayList<BudgetModel>) budgetRepository.getAllBudgets();

        BudgetRVAdapter budgetRVAdapter = new BudgetRVAdapter(budgetModelArrayList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        budgetRv.setLayoutManager(linearLayoutManager);
        budgetRv.setAdapter(budgetRVAdapter);

        budgetRVAdapter.setOnClickListener(position -> {
            BudgetModel model = budgetModelArrayList.get(position);
            String name = model.getName();
            double money = model.getAmount();
            int id = model.getId();
            // String description = model.getBudgetDescription(); // BudgetModel does not have a description
            // dung Intent + Bundle de truyen data sang EditBudget
            Toast.makeText(getActivity(), name, Toast.LENGTH_SHORT).show();
        });

        btnCreteBudget.setOnClickListener(v -> {
            // chuyen sang man add budget
            Intent intent = new Intent(getActivity(), AddBudgetActivity.class);
            startActivity(intent);
        });
        return view;
    }
}