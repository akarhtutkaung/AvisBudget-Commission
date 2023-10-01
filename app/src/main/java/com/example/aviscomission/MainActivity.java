package com.example.aviscomission;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText coverageEditText, upsellsEditText, accGSOEditText;
    private TextView resultTotal, resultDetails;

    final static double[][][] Bucket = {                         // [Bucket_Num][Tier][Min,Max]
            { // Bucket 0 (Coverage)
                    {2999.99, 2999.99, 3.0},            // Tier 1
                    {3000.00, 14999.99, 6.0},           // Tier 2
                    {15000.00, 25999.99, 10.0},         // Tier 3
                    {26000.00, 37999.99, 15.0},         // Tier 4
                    {38000.00, Double.MAX_VALUE, 20.0}  // Tier 5
            },
            { // Bucket 1 (Upsells & Walk-ups)
                    {3499.99, 3499.99, 3.0},            // Tier 1
                    {3500.00, 14999.99, 6.0},           // Tier 2
                    {15000.00, 26999.99, 10.0},         // Tier 3
                    {27000.00, 38999.99, 15.0},         // Tier 4
                    {39000.00, Double.MAX_VALUE, 20.0}  // Tier 5
            },
            { // Bucket 2 (Acc & GSO)
                    {2999.99, 2999.99, 3.0},            // Tier 1
                    {3000.00, 9999.99, 6.0},            // Tier 2
                    {10000.00, 17999.99, 10.0},         // Tier 3
                    {18000.00, 24999.99, 15.0},         // Tier 4
                    {25000.00, Double.MAX_VALUE, 20.0}  // Tier 5
            }
    };
    final static int TotalTier = 5;
    final static int TotalBucket = 3;
    static double[][] AmountTier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AmountTier = new double[TotalBucket][TotalTier];
        CalculateAmountEachTier();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coverageEditText = findViewById(R.id.coverageEditText);
        upsellsEditText = findViewById(R.id.upsellsEditText);
        accGSOEditText = findViewById(R.id.accGSOEditText);
        resultTotal = findViewById(R.id.resultTotal);
        resultDetails = findViewById(R.id.resultDetails);

        Button calculateButton = findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculatePayout();
            }
        });
    }

    private void calculatePayout() {
        String coverageText = coverageEditText.getText().toString();
        String upsellsText = upsellsEditText.getText().toString();
        String accGSOText = accGSOEditText.getText().toString();

        if (TextUtils.isEmpty(coverageText) || TextUtils.isEmpty(upsellsText) || TextUtils.isEmpty(accGSOText)) {
            resultTotal.setText("Please enter values for all fields.");
            resultDetails.setText("");
            return;
        }

        double made_coverage = Double.parseDouble(coverageText);
        double made_upsell_walk = Double.parseDouble(upsellsText);
        double made_acc_gas = Double.parseDouble(accGSOText);

        int cut_off_coverage = GetCutOff(made_coverage, 0);
        int cut_off_upsell_walk = GetCutOff(made_upsell_walk, 1);
        int cut_off_acc_gas = GetCutOff(made_acc_gas, 2);

        double payout_coverage = CalculatePayOut(0, cut_off_coverage, made_coverage);
        double payout_upsell_walk = CalculatePayOut(1, cut_off_upsell_walk, made_upsell_walk);
        double payout_acc_gas = CalculatePayOut(2, cut_off_acc_gas, made_acc_gas);

        double total = payout_coverage + payout_upsell_walk + payout_acc_gas;
        DecimalFormat df = new DecimalFormat("0.00");

        StringBuilder result_total = new StringBuilder();
        StringBuilder result_detail = new StringBuilder();
        result_total.append("Total: $" + df.format(total));

        result_detail.append("Coverage: $" + df.format(payout_coverage) + "\n");
        result_detail.append("Upsells & Walkups: $" + df.format(payout_upsell_walk) + "\n");
        result_detail.append("Acc & GSO: $" + df.format(payout_acc_gas) + "\n");

        resultTotal.setText(result_total.toString());
        resultDetails.setText(result_detail.toString());
    }

    /**
     * Get the number of tier that is hit.
     *
     * @param made Amount of money we made for specific tier.
     * @param bucketN The bucket number.
     * @return Tier number hit with the given amount.
     */
    public static int GetCutOff(double made, int bucketN) {
        for(int i=TotalTier-1; i>=0; i--) {
            if(made >= Bucket[bucketN][i][0]) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Calculate the payout for given bucket.
     *
     * @param bucketN Bucket number.
     * @param cutOff Max hit tier.
     * @param made Amount made for the bucket.
     * @return Payout for the given bucket.
     */
    public static double CalculatePayOut(int bucketN, int cutOff, double made) {
        int indexTierCut = cutOff - 1;
        if(cutOff == 0) {
            return 0;
        } else if (cutOff == 1) {
            return Bucket[bucketN][indexTierCut][0] * Bucket[bucketN][indexTierCut][2] * 0.01;
        }

        double total = (made - Bucket[bucketN][indexTierCut][0]) * Bucket[bucketN][indexTierCut][2]; // Calculate the highest tier payout.
        for(int i=0; i<indexTierCut; i++) {
            total += AmountTier[bucketN][i] * Bucket[bucketN][i][2];
        }

        return total * 0.01;
    }

    /**
     * Calculate the amount of payout for each tier for each bucket.
     *
     * @return Array of payouts for each tier for each bucket.
     */
    public static void CalculateAmountEachTier() {
        for(int i=0; i<3; i++) {
            AmountTier[i][0] = Bucket[i][0][0];
            for(int j=1; j<TotalTier-1; j++) {
                AmountTier[i][j] = Bucket[i][j][1] - Bucket[i][j][0];
            }
        }
    }
}
