package model;

public class utils {
    public utils(){}

    //求出3个点中值最小的那一个
    public double find_min_num3(double e1, double e2, double e3){
        if(e1 < e2){
            if(e1 < e3)
                return e1;
            else
                return e3;
        }
        else{
            if(e2 < e3)
                return e2;
            else
                return e3;
        }
    }

    //求出2个点中值最小的那一个
    public double find_min_num2(double e1, double e2){
        if(e1 < e2)
            return e1;
        else
            return e2;
    }

    //求出3个点中哪个最小
    public int find_min_index3(double e1, double e2, double e3){
        if(e1 < e2){
            if(e1 < e3)
                return -1;
            else
                return 1;
        }
        else{
            if(e2 < e3)
                return 0;
            else
                return 1;
        }
    }

    public static class bonding_box{
        //类成员
        //点1是左上角的点
        //点2是右下角点点
        private int x1;
        private int y1;
        private int x2;
        private int y2;

        public bonding_box(int x1, int y1, int x2, int y2){
            if(x2 <= x1){
                System.out.println("In constructor bonding_box, the x values are invalid");
                return;
            }
            if(y2 <= y1){
                System.out.println("In constructor bonding_box, the y values are invalid");
            }
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public int[] info(){
            int[] box = new int[4];
            box[0] = this.x1;
            box[1] = this.y1;
            box[2] = this.x2;
            box[3] = this.y2;
            return box;
        }

        public void modify(int x1, int y1, int x2, int y2){
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    public static int[][][] BiCubicExpand(int[][][] matrix, int result_height, int result_width) {
        int origin_height = matrix.length;
        int origin_width = matrix[0].length;
        int[][][] result_matrix = new int[result_height][result_width][3];
        float xRatio = (float) (origin_width - 1) / result_width;
        float yRatio = (float) (origin_height - 1) / result_height;
        for (int i = 0; i < result_height; i++) {
            for (int j = 0; j < result_width; j++) {
                float x = xRatio * j;
                float y = yRatio * i;
                int xInt = (int) x;
                int yInt = (int) y;
                float xDiff = x - xInt;
                float yDiff = y - yInt;
                float[] result = new float[3];
                for (int m = -1; m <= 2; m++) {
                    for (int n = -1; n <= 2; n++) {
                        int[] pixels = getPixels(matrix, xInt + m, yInt + n);
                        result[0] += pixels[0] * BiCubicKernel(m - xDiff) * BiCubicKernel(yDiff - n);
                        result[1] += pixels[1] * BiCubicKernel(m - xDiff) * BiCubicKernel(yDiff - n);
                        result[2] += pixels[2] * BiCubicKernel(m - xDiff) * BiCubicKernel(yDiff - n);
                    }
                }
                result_matrix[i][j][0] = Math.min(Math.max((int) result[0], 0), 255);
                result_matrix[i][j][1] = Math.min(Math.max((int) result[1], 0), 255);
                result_matrix[i][j][2] = Math.min(Math.max((int) result[2], 0), 255);
            }
        }
        return result_matrix;
    }

    private static float BiCubicKernel(float x) {
        float a = -0.5f;
        if (x < 0)
            x = -x;
        float x2 = x * x;
        float x3 = x2 * x;
        if (x <= 1) {
            return (a + 2) * x3 - (a + 3) * x2 + 1;
        } else if (x <= 2) {
            return a * x3 - 5 * a * x2 + 8 * a * x - 4 * a;
        } else {
            return 0;
        }
    }

    private static int[] getPixels(int[][][] matrix, int x, int y) {
        int height = matrix.length;
        int width = matrix[0].length;
        if (x < 0)
            x = 0;
        if (x >= width)
            x = width - 1;
        if (y < 0)
            y = 0;
        if (y >= height)
            y = height - 1;
        return matrix[y][x];
    }

}
