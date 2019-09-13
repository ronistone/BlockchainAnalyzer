import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.blockchain.api.APIException;
import info.blockchain.api.blockexplorer.BlockExplorer;
import info.blockchain.api.blockexplorer.entity.Address;
import info.blockchain.api.blockexplorer.entity.Balance;
import info.blockchain.api.blockexplorer.entity.Block;
import info.blockchain.api.blockexplorer.entity.FilterType;
import info.blockchain.api.blockexplorer.entity.Input;
import info.blockchain.api.blockexplorer.entity.Output;
import info.blockchain.api.blockexplorer.entity.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {


    private Gson gson = new GsonBuilder().create();
    private BlockExplorer blockExplorer = new BlockExplorer();
    private Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {

        Main main = new Main();


        while(true){

            try {
                main.executeTheMenuLoop();
            } catch (APIException e) {
                System.err.println("I hava catch an Api exception!!");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("I hava catch an IOException exception!!");
                e.printStackTrace();
            }

        }

    }

    private void executeTheMenuLoop() throws APIException, IOException {
        Long height;
        String hash;
        System.out.println("Menu:\n1 - get Blocks by Height\n2 - get transactions\n3 - Get Address(Wallet)\n4 - Show All Wallet Balance in blocks with max height\noption: ");
        int option = scanner.nextInt();

        switch (option){
            case 1:
                System.out.print("Insert the height: ");
                height = scanner.nextLong();
                List<Block> blockList = blockExplorer.getBlocksAtHeight(height);
                System.out.println(gson.toJson(blockList));
                break;
            case 2:
                scanner.nextLine();
                System.out.print("Insert the transaction hash: ");
                hash = scanner.nextLine();
                Transaction transaction = blockExplorer.getTransaction(hash);
                System.out.println(gson.toJson(transaction));
                break;
            case 3:
                scanner.nextLine();
                System.out.print("Insert the Address hash: ");
                hash = scanner.nextLine();
                Address address = blockExplorer.getAddress(hash);

                System.out.println(gson.toJson(address));
                break;
            case 4:
                scanner.nextLine();
                System.out.print("Looking for all wallets in blocks up to height...\nPlease insert the height: ");
                height = scanner.nextLong();
                System.out.println("Insert min block height");
                Long heightMin = scanner.nextLong();

                showAllBalanceInHeight(height, heightMin);
                break;
        }
    }

    private void showAllBalanceInHeight(Long height, Long heightMin) throws APIException, IOException {

        Set<String> wallets = new HashSet<String>();

        for(long i=heightMin;i<height;i++){

            List<Block> blocks = blockExplorer.getBlocksAtHeight(i);

            for(Block block: blocks){

                for(Transaction transaction: block.getTransactions()){

                    for(Input input: transaction.getInputs()){
                        if(input != null && input.getPreviousOutput() != null) {
                            wallets.add(input.getPreviousOutput().getAddress());
                        }
                    }

                    for(Output output: transaction.getOutputs()){
                        wallets.add(output.getAddress());
                    }

                }

            }


        }

        Map<String, Balance> balanceWallets = blockExplorer.getBalance(new ArrayList<String>(wallets), FilterType.All);

        System.out.println("Wallets: (" + balanceWallets.size() +" wallets found)");


        for(Map.Entry<String, Balance> balanceEntry: balanceWallets.entrySet()){
            Balance oldBalance = balanceEntry.getValue();
            MyBalance balance = new MyBalance(
                    (double) oldBalance.getFinalBalance() / 100000000d,
                    oldBalance.getTxCount(),
                    (double) oldBalance.getTotalReceived() / 100000000d
            );

            System.out.println(balanceEntry.getKey() + "  ->  " + gson.toJson(balance));
        }

    }


    private static class MyBalance{


        public Double finalBalance;
        public Long txCount;
        public Double totalReceived;

        public MyBalance(Double finalBalance, Long txCount, Double TotalReceived) {

            this.finalBalance = finalBalance;
            this.txCount = txCount;
            totalReceived = TotalReceived;
        }

    }

}
