package com.example.ballotblock.Pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ballotblock.R;
import com.example.ballotblock.navigation.Main_Profile;
import com.example.ballotblock.navigation.MapsActivity;
import com.example.ballotblock.navigation.VoteNow;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Wallet extends AppCompatActivity {
    Toolbar toolbar;
    EditText walletAddress;
    public Web3j client;
    String URL;
    public String ethAddress;
    String password, walletPath;
    File walletDir;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        walletAddress = findViewById(R.id.CreateWalletTxt);

        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
//        for back button, we can also customize back btn - to customize see link bookmarked
        getSupportActionBar().setTitle("Wallet");

//        Bottom Navigation Bar
        BottomNavigationView bottomNavigationView =  findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.wallet);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.home_page:
                        Intent intent = new Intent(getApplicationContext(), HomePage.class);
                        startActivity(intent);
//                        Toast.makeText(getApplicationContext(), "Home page", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onNavigationItemSelected: Home Page");
                        return true;
                    case R.id.election:
//                        Toast.makeText(getApplicationContext(), "Elections", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onNavigationItemSelected: Elections");
                        Intent intent1 = new Intent(getApplicationContext(), ElectionType.class);
                        startActivity(intent1);
                        return true;
                    case  R.id.wallet:
//                        Toast.makeText(getApplicationContext(), "Wallet", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onNavigationItemSelected: Wallet");
                        Intent intent2 = new Intent(getApplicationContext(), Wallet.class);
                        startActivity(intent2);
                        return true;
                    case R.id.profile:
//                        Toast.makeText(getApplicationContext(), "Profile", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onNavigationItemSelected: Profile");
                        Intent intent3 = new Intent(getApplicationContext(), Profile.class);
                        startActivity(intent3);
                        return true;
                    case R.id.Map:
//                        Toast.makeText(getApplicationContext(), "Map", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "onNavigationItemSelected: Map");
                        Intent intent4 = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent4);
                        return true;
                }
                return false;
            }
        });

//        for creating wallet error - saw from github
        setupBouncyCastle();

//        Infura Rinkeby Network url
        URL = "https://rinkeby.infura.io/v3/6c60a808c9c54857a88cc2a8f969d5a8";

        client = Web3j.build(new HttpService(URL));
//        by Default to check balance of any eth account mainnet/rinkeby
        ethAddress = "0x4063a1c143fc2cdb10447db65e6d61f7e241e98b";

//        for Wallet Credentails and loading
        walletPath = getFilesDir().getAbsolutePath();
        walletDir  = new File(walletPath);
    }

    public void getBalance(Web3j client, String ethAddress) {
        ethAddress = "0xD47B9fC80449C5EDA71746a90C5F5a0870148fc5";
        EthGetBalance balanceResponse = null;
        try {
            balanceResponse = client.ethGetBalance(ethAddress, DefaultBlockParameter.valueOf("latest")).sendAsync().get(60, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        final BigInteger unscaledBalance = balanceResponse.getBalance();
        final BigDecimal scaledBalance = new BigDecimal(unscaledBalance).divide(new BigDecimal(1000000000000000000L), 18, RoundingMode.HALF_UP);
        Log.d("tagg", "Eth Balance: " + String.valueOf(scaledBalance));
        Toast.makeText(this, "Eth Balance: " + scaledBalance, Toast.LENGTH_LONG).show();
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public void CreateWallet(View view) {
//        Wallet Creation
        password = "Pass123";

        try{
            fileName =  WalletUtils.generateLightNewWalletFile(password,walletDir);
            Log.d("tagg","FileName: " + fileName);
            walletDir = new File(walletPath + "/" + fileName);
            Log.d("tagg", "Wallet Directory: " + String.valueOf(walletDir));

            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
            Toast.makeText(this, "Your New Wallet Address is " + credentials.getAddress(), Toast.LENGTH_LONG).show();
            Log.d("tagg", "New Wallet Address: " + credentials.getAddress());
            walletAddress.setText(credentials.getAddress());
            ethAddress = credentials.getAddress();
        }
        catch (Exception e){
            Log.d("tagg", "Wallet not created");
        }

    }

    public void MyAddress(View view) {
//        Load Wallet Credentials
        password = "Pass123";
//        walletPath = getFilesDir().getAbsolutePath();
//        walletDir  = new File(walletPath);

        walletDir = new File("/data/user/0/com.example.ballotblock/files/UTC--2022-04-16T13-24-46.130000000Z--d47b9fc80449c5eda71746a90c5f5a0870148fc5.json");

        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
            Toast.makeText(this, "Your address is " + credentials.getAddress(), Toast.LENGTH_LONG).show();
            Log.d("tagg", "Wallet Address: " + credentials.getAddress());
        }
        catch (Exception e){
            Toast.makeText(this, "Could not Fetch Address... ", Toast.LENGTH_LONG).show();
        }

    }

    public void GetBalance(View view) {
        getBalance(client, ethAddress);
    }


    public void SendEther(View view) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
            TransactionReceipt receipt = Transfer.sendFunds(client, credentials, "0x8c1317F9C8Eab696DFa6E854b2Da49D0FC652a1E", new BigDecimal(1), Convert.Unit.ETHER).sendAsync().get();
            Toast.makeText(this, "Transaction complete: " + receipt.getTransactionHash(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "Could not send Ether, " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Could not send Ether, " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("tagg", "Could not send Ether, " + e.getLocalizedMessage());
        }
    }
}