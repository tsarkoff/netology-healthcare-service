package ru.netology.patient.service.alert;

public class SendAlertServiceImpl implements SendAlertService {

    @Override
    public SendAlertService send(String message) {
        System.out.println(message);
        return null;
    }
}
