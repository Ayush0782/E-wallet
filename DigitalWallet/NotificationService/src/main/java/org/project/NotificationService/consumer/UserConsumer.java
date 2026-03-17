package org.project.NotificationService.consumer;

import org.json.JSONObject;
import org.project.NotificationService.worker.EmailWorker;
import org.project.enums.UserIdentifier;
import org.project.util.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    @Autowired
    EmailWorker emailWorker;

    @KafkaListener(topics = "USER_DETAILS_QUEUE", groupId = "email_notification")
    public void listenNewlyCreatedConsumer(String data){
        System.out.println(data);

        JSONObject jsonObject = new JSONObject(data);
         String email = jsonObject.getString(CommonConstants.USER_EMAIL);
         String name = jsonObject.getString(CommonConstants.USER_NAME);
         String userIdentifierValue = jsonObject.getString(CommonConstants.USER_IDENTIFIER_VALUE);

         UserIdentifier userIdentifier = jsonObject.getEnum(UserIdentifier.class,CommonConstants.USER_IDENTIFIER);

         emailWorker.sendEmail(email, name, userIdentifierValue , userIdentifier.toString());

          System.out.println("Email Sent to User");

    }
}
