package co.polarr.albumsdkdemo.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import co.polarr.processing.entities.ResultItem;

/**
 * Created by Colin on 2017/11/29.
 * output rating results to an csv file
 */

public class ExportUtil {
    public static void ExportToCsv(List<ResultItem> results, File targetFile) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("File Name,Clarity,Exposure,Colorfulness,Emotion,Overall\n");

            for (ResultItem result : results) {
                if (result.filePath != null) {
                    sb.append(new File(result.filePath).getName());
                }
                sb.append(",");

                if (result.features.containsKey("metric_clarity")) {
                    sb.append(result.features.get("metric_clarity"));
                }
                sb.append(",");

                if (result.features.containsKey("metric_exposure")) {
                    sb.append(result.features.get("metric_exposure"));
                }
                sb.append(",");


                if (result.features.containsKey("metric_colorfulness")) {
                    sb.append(result.features.get("metric_colorfulness"));
                }
                sb.append(",");


                if (result.features.containsKey("metric_emotion")) {
                    float emotion = (float) result.features.get("metric_emotion");
                    if (emotion > 0) {
                        sb.append(emotion);
                    }
                }
                sb.append(",");


                if (result.features.containsKey("rating_all")) {
                    sb.append(result.features.get("rating_all"));
                }

                sb.append("\n");
            }

            BufferedWriter bwr = new BufferedWriter(new FileWriter(targetFile));
            bwr.write(sb.toString());
            bwr.flush();
            bwr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
