package org.openmrs.mobile.activities.ecganalysis;

/**
 * Created by HP on 26-02-2018.
 */
import org.openmrs.mobile.activities.ecganalysis.QRSDetector2;
import org.openmrs.mobile.activities.ecganalysis.QRSDetectorParameters;
import org.openmrs.mobile.activities.ecganalysis.QRSFilterer;

public class OSEAFactory {

    /**
     * Create a QRSDetector2 for the given sampleRate
     *
     * @param sampleRate The sampleRate of the ECG samples
     * @return A QRSDetector2
     */
    public static QRSDetector2 createQRSDetector2(int sampleRate)
    {
        QRSDetectorParameters qrsDetectorParameters = new QRSDetectorParameters(sampleRate) ;

        QRSDetector2 qrsDetector2 = new QRSDetector2(qrsDetectorParameters) ;
        QRSFilterer qrsFilterer = new QRSFilterer(qrsDetectorParameters) ;

        qrsDetector2.setObjects(qrsFilterer) ;
        return qrsDetector2 ;
    }
}
