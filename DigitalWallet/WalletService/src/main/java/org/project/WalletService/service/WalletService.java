package org.project.WalletService.service;


import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.project.WalletService.model.Wallet;
import org.project.WalletService.repository.WalletRepository;
import org.project.enums.TxnStatus;
import org.project.enums.UserStatus;
import org.project.util.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {



    @Value("${wallet.initial.amount}")
    private String walletAmount;

    @Autowired
    WalletRepository walletRepository;

    TxnStatus txnStatus;

    String txnMessage;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public void createWalletAccount(Wallet wallet){
        wallet.setUserStatus(UserStatus.ACTIVE);
        wallet.setBalance(Double.parseDouble(walletAmount));

        walletRepository.save(wallet);

        System.out.println("Wallet account created");

    }

    public void processNewTransaction(String senderId, String receiverId, double amount, String txnId){
        Wallet sender = walletRepository.findByMobileNo(senderId);
        Wallet receiver = walletRepository.findByMobileNo(receiverId);

        if (sender == null || sender.getUserStatus() != UserStatus.ACTIVE){
            txnStatus = TxnStatus.FAILED;
            txnMessage = "Sender wallet doesn't exist";
        } else if (receiver == null || receiver.getUserStatus() != UserStatus.ACTIVE) {
            txnStatus = TxnStatus.FAILED;
            txnMessage = "Receiver wallet doesn't exist";
        }
        else {
            if (sender.getBalance()<amount){
                txnStatus = TxnStatus.FAILED;
                txnMessage = "Insufficient Balance";
            }
            else{
                if (processAmount(senderId,receiverId,amount)){
                    txnStatus = TxnStatus.SUCCESS;
                    txnMessage = "Transaction Is Success";
                }else {
                    txnStatus = TxnStatus.PENDING;
                    txnMessage = "Transaction is Pending";
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommonConstants.TXN_ID, txnId);
        jsonObject.put(CommonConstants.TXN_STATUS,txnStatus);
        jsonObject.put(CommonConstants.TXN_MESSAGE,txnMessage);

        kafkaTemplate.send(CommonConstants.TXN_UPDATE_QUEUE_TOPIC, jsonObject.toString());
        System.out.println("Updated transaction Details sent to kafka");

    }

    @Transactional
    public boolean processAmount(String senderId, String receiverId, double amount){
        boolean isDone = false;
        synchronized (this){
            try {
                walletRepository.updateWallet(senderId, -amount);
                walletRepository.updateWallet(receiverId, amount);
                isDone = true;
            } catch (Exception ex){
                isDone = false;
            }
        }
        return isDone;
    }

    public Double getWalletBalance(String username){
        Wallet wallet = walletRepository.findByMobileNo(username);
        if (wallet == null){
            return null;
        }
        return wallet.getBalance();

    }

}
