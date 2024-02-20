package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class MedicalServiceImplTest {
    @Mock
    public PatientInfoRepository patientInfoRepository;
    @Mock
    public SendAlertService sendAlertService;
    @InjectMocks
    public MedicalServiceImpl medicalService;

    @ParameterizedTest
    @CsvSource(value = {
            // HELP, highCurr, lowCurr, id, name, surname, birthday, tempNorm, highNorm, lowNorm
            // давление в норме у Пациента-1: HELP вызван 0 раз = sendAlertService
            "0, 120, 80, 1, Иван, Петров, 1980-11-26, 36.65, 120, 80",
            // верхнее повышено у Пациента-1: HELP вызван 1 раз = sendAlertService
            "1, 170, 80, 1, Иван, Петров, 1980-11-26, 36.65, 120, 80",
            // давление в норме у Пациента-2: HELP вызван 0 раз = sendAlertService
            "0, 125, 78, 2, Семен, Михайлов, 1982-01-16, 36.6, 125, 78",
            // нижнее повышено у Пациента-2: HELP вызван 1 раз = sendAlertService
            "1, 125, 90, 2, Семен, Михайлов, 1982-01-16, 36.6, 125, 78"
    })
    void checkBloodPressure(int help, int highCurr, int lowCurr, String id, String name, String surname, String birthday, String tempNorm, int highNorm, int lowNorm) {
        PatientInfo pi = new PatientInfo(name, surname, LocalDate.parse(birthday), new HealthInfo(new BigDecimal(tempNorm), new BloodPressure(highNorm, lowNorm)));
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(pi);
        medicalService.checkBloodPressure(id, new BloodPressure(highCurr, lowCurr));
        Mockito.verify(sendAlertService, Mockito.times(help)).send(Mockito.anyString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // HELP, tempCurr, id, name, surname, birthday, tempNorm, highNorm, lowNorm
            // температура в норме у Пациента-1: HELP вызван 0 раз = sendAlertService
            "0, 36.6, 1, Иван, Петров, 1980-11-26, 36.65, 120, 80",
            // температура повышена у Пациента-1: HELP вызван 1 раз = sendAlertService
            "1, 38.0, 1, Иван, Петров, 1980-11-26, 36.65, 120, 80",
            // температура в норме у Пациента-2: HELP вызван 0 раз = sendAlertService
            "0, 36.6, 2, Семен, Михайлов, 1982-01-16, 36.6, 125, 78",
            // температура понижена у Пациента-2: HELP вызван 1 раз = sendAlertService
            "1, 35.0, 2, Семен, Михайлов, 1982-01-16, 36.6, 125, 78"
    })
    void checkTemperature(int help, String tempCurr, String id, String name, String surname, String birthday, String tempNorm, int highNorm, int lowNorm) {
        PatientInfo pi = new PatientInfo(id, name, surname, LocalDate.parse(birthday), new HealthInfo(new BigDecimal(tempNorm), new BloodPressure(highNorm, lowNorm)));
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(pi);
        medicalService.checkTemperature(id, new BigDecimal(tempCurr));
        Mockito.verify(sendAlertService, Mockito.times(help)).send(Mockito.anyString());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // id, highCurr, lowCurr, highNorm, lowNorm
            "1, 120, 80, 120, 60",
            "2, 120, 80, 170, 80"
    })
    void checkSendAlertPressure(String id, int highCurr, int lowCurr, int highNorm, int lowNorm) {
        PatientInfo pi = new PatientInfo(id, null, null, null, new HealthInfo(null, new BloodPressure(highNorm, lowNorm)));
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(pi);
        medicalService.checkBloodPressure(id, new BloodPressure(highCurr, lowCurr));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        Assertions.assertEquals(String.format("Warning, patient with id: %s, need help", pi.getId()), argumentCaptor.getValue());
    }

    @ParameterizedTest
    @CsvSource(value = {
            // id, tempCurr, tempNorm
            "1, 38.0, 36.65",
            "2, 35.0, 36.6"
    })
    void checkSendAlertTemperature(String id, String tempCurr, String tempNorm) {
        PatientInfo pi = new PatientInfo(id, null, null, null, new HealthInfo(new BigDecimal(tempNorm), null));
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(pi);
        medicalService.checkTemperature(id, new BigDecimal(tempCurr));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        Assertions.assertEquals(String.format("Warning, patient with id: %s, need help", pi.getId()), argumentCaptor.getValue());
    }
}