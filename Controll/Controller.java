package Controll;

import model.seam_carving_rgb;
import view.ImageProcessingGUI;

import java.util.ArrayList;

public class Controller {
    public static void main(String[] args) {
        ImageProcessingGUI gui = new ImageProcessingGUI();//初始化界面代码
        boolean finished = false;
        while (true) {
            while (true) {
                if (gui.startProcessing()) {
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }//判断是否开始处理

            int[][][] matrix = gui.convertToRGBMatrix();
            int[][][] resultImage = null;

            seam_carving_rgb originalImage = new seam_carving_rgb(matrix);

            //存储erase和protect区域
            ArrayList<Integer> interestPoints = new ArrayList<Integer>();
            ArrayList<Integer> unInterestPoints = new ArrayList<Integer>();
            if (gui.isInterestedArea())
                interestPoints = gui.selectInterestedArea();
            if (gui.isUninterestedArea())
                unInterestPoints = gui.selectUninterestedArea();

            resultImage = originalImage.resize(gui.expectedSize()[1], gui.expectedSize()[0], interestPoints, unInterestPoints);

            //处理完了，调用displayProcessedImage显示图像
            gui.displayProcessedImage(resultImage);
        }
    }
}