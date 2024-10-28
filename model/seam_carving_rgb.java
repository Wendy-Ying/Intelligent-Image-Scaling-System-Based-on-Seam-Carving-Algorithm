package model;

import java.util.ArrayList;
import java.util.Arrays;

public class seam_carving_rgb {
    //类的成员
    private int[][][] image; //原始图像
    private int cur_height; //原始图像的高，对应三维矩阵中的第一维
    private int cur_width;  //原始图像的宽，对应三维矩阵中的第二维
    private int res_height;
    private int res_width;
    private ArrayList<Integer> protected_area;
    private ArrayList<Integer> removal_area;

    //构造器，用于创建seam_vertical的实例对象
    //传入需要被处理的origin_image
    public seam_carving_rgb(int[][][] origin_image) {
        this.image = origin_image;
        this.cur_height = origin_image.length;
        this.cur_width = origin_image[0].length;
        this.protected_area = null;
        this.removal_area = null;
    }

    //实现的主要功能
    public int[][][] resize(int result_height, int result_width, ArrayList<Integer> protected_area, ArrayList<Integer> removal_area) {
        //height，width正确性检验，不能为负数
        if (result_height <= 0 || result_width <= 0) { //这里抛出异常的函数不会写
            System.out.println("In seam_carving.resize(), the result_size of the function is invalid");
            return null;
        }
        this.protected_area = protected_area;
        this.removal_area = removal_area;
        this.res_height = result_height;
        this.res_width = result_width;

        //扩张图片
        double[][] energy_map = energy_map();
        double[][] accumulated_map_horizontal = null;
        double[][] accumulated_map_vertical = null;
        if (this.res_height > this.cur_height) {
            accumulated_map_horizontal = accumulated_energy_map_horizontal(energy_map);
            expand_horizontal(accumulated_map_horizontal);
        }
        if (this.res_width > this.cur_width) {
            energy_map = energy_map();
            accumulated_map_vertical = accumulated_energy_map_vertical(energy_map);
            expand_vertical(accumulated_map_vertical);
        }

        //缩减图片
        energy_map = energy_map();
        while (this.res_height != this.cur_height || this.res_width != this.cur_width) {
            if (this.res_height < this.cur_height && this.res_height < this.cur_width) {
                accumulated_map_horizontal = accumulated_energy_map_horizontal(energy_map);
                accumulated_map_vertical = accumulated_energy_map_vertical(energy_map);

                //找到最低的累积能量值
                int last_col = this.cur_width - 1;
                int last_row = this.cur_height - 1;
                int flag = 0; //若flag==0，则选择horizontal，若为flag==1，则选择vertical
                int index = 0;

                //找到最佳切除横裂缝的地方
                double minTotalEnergy = Double.MAX_VALUE;
                minTotalEnergy = accumulated_map_horizontal[0][last_col - 1];
                for (int i = 1; i < this.cur_height; i++) {
                    if (accumulated_map_horizontal[i][last_col - 1] < minTotalEnergy) {
                        minTotalEnergy = accumulated_map_horizontal[i][last_col - 1];
                        flag = -1;
                        index = i;
                    }
                }
                //找到最佳切除竖裂缝的地方
                for (int j = 0; j < this.cur_width; j++) {
                    if (accumulated_map_vertical[last_row - 1][j] < minTotalEnergy) {
                        minTotalEnergy = accumulated_map_vertical[last_row - 1][j];
                        flag = 1;
                        index = j;
                    }
                }

                //切除
                if (flag == -1) {
                    int[] path = new int[this.cur_width];
                    path = find_min_energy_path_horizontal(accumulated_map_horizontal, index);
                    energy_map = cut_1step_horizontal(path, energy_map);
                } else if (flag == 1) {
                    int[] path = new int[this.cur_height];
                    path = find_min_energy_path_vertical(accumulated_map_vertical, index);
                    energy_map = cut_1step_vertical(path, energy_map);
                }
            } else if (this.res_height >= this.cur_height && this.res_width < this.cur_width) {
                accumulated_map_vertical = accumulated_energy_map_vertical(energy_map);

                //找到最低的累积能量值
                int last_row = this.cur_height - 1;
                int index = 0;
                double minTotalEnergy = Double.MAX_VALUE;

                for (int j = 0; j < this.cur_width; j++) {
                    if (accumulated_map_vertical[last_row - 1][j] < minTotalEnergy) {
                        minTotalEnergy = accumulated_map_vertical[last_row - 1][j];
                        index = j;
                    }
                }

                int[] path = new int[this.cur_height];
                path = find_min_energy_path_vertical(accumulated_map_vertical, index);
                energy_map = cut_1step_vertical(path, energy_map);
            } else if (this.res_height < this.cur_height && this.res_width >= this.cur_width) {
                accumulated_map_horizontal = accumulated_energy_map_horizontal(energy_map);

                //找到最低的累积能量值
                int last_col = this.cur_width - 1;
                int index = 0;
                double minTotalEnergy = Double.MAX_VALUE;

                for (int i = 0; i < this.cur_height; i++) {
                    if (accumulated_map_vertical[i][last_col - 1] < minTotalEnergy) {
                        minTotalEnergy = accumulated_map_vertical[i][last_col - 1];
                        index = i;
                    }
                }

                int[] path = new int[this.cur_width];
                path = find_min_energy_path_horizontal(accumulated_map_horizontal, index);
                energy_map = cut_1step_horizontal(path, energy_map);
            }
        }

        return this.image;
    }

    //删除掉能量最小的横裂缝
    private double[][] cut_1step_horizontal(int[] path, double[][] energy_map) {
        int[][][] result_matrix = new int[this.cur_height - 1][this.cur_width][3];
        double[][] result_energy_map = new double[this.cur_height - 1][this.cur_width];
        for (int j = 0; j < this.cur_width; j++) {
            int cut_index = path[j];
            for (int i = 0; i < this.cur_height; i++) {
                if (i < cut_index - 1) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];

                    result_energy_map[i][j] = energy_map[i][j];
                } else if (i == cut_index - 1) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];

                    if (energy_map[i][j] > 10000 || energy_map[i][j] < 0) {
                        result_energy_map[i][j] = energy_map[i][j];
                    } else {
                        if (j == 0 || i == 0 || j == this.cur_width - 1 || i == this.cur_height - 1) {
                            result_energy_map[i][j] = 1000;
                        } else {
                            int rx = this.image[i][j + 1][0] - this.image[i][j - 1][0];
                            int gx = this.image[i][j + 1][1] - this.image[i][j - 1][1];
                            int bx = this.image[i][j + 1][2] - this.image[i][j - 1][2];

                            int ry = this.image[i + 2][j][0] - this.image[i - 1][j][0];
                            int gy = this.image[i + 2][j][1] - this.image[i - 1][j][1];
                            int by = this.image[i + 2][j][2] - this.image[i - 1][j][2];

                            result_energy_map[i][j] = Math.sqrt(rx * rx + gx * gx + bx * bx + ry * ry + gy * gy + by * by);
                        }
                    }
                } else if (i == cut_index + 1) {
                    result_matrix[i - 1][j][0] = this.image[i][j][0];
                    result_matrix[i - 1][j][1] = this.image[i][j][1];
                    result_matrix[i - 1][j][2] = this.image[i][j][2];

                    if (energy_map[i][j] > 10000 || energy_map[i][j] < 0) {
                        result_energy_map[i - 1][j] = energy_map[i][j];
                    } else {
                        if (j == 0 || i == 0 || j == this.cur_width - 1 || i == this.cur_height - 1) {
                            result_energy_map[i - 1][j] = 1000;
                        } else {
                            int rx = this.image[i][j + 1][0] - this.image[i][j - 1][0];
                            int gx = this.image[i][j + 1][1] - this.image[i][j - 1][1];
                            int bx = this.image[i][j + 1][2] - this.image[i][j - 1][2];

                            int ry = this.image[i + 1][j][0] - this.image[i - 2][j][0];
                            int gy = this.image[i + 1][j][1] - this.image[i - 2][j][1];
                            int by = this.image[i + 1][j][2] - this.image[i - 2][j][2];

                            result_energy_map[i - 1][j] = Math.sqrt(rx * rx + gx * gx + bx * bx + ry * ry + gy * gy + by * by);
                        }
                    }
                } else if (i > cut_index + 1) {
                    result_matrix[i - 1][j][0] = this.image[i][j][0];
                    result_matrix[i - 1][j][1] = this.image[i][j][1];
                    result_matrix[i - 1][j][2] = this.image[i][j][2];

                    result_energy_map[i - 1][j] = energy_map[i][j];
                }
            }
        }
        this.cur_height = this.cur_height - 1;
        this.image = result_matrix;
        return result_energy_map;
    }

    //删除掉能量最小的横裂缝
    private double[][] cut_1step_vertical(int[] path, double[][] energy_map) {
        int[][][] result_matrix = new int[this.cur_height][this.cur_width - 1][3];
        double[][] result_energy_map = new double[this.cur_height][this.cur_width - 1];
        for (int i = 0; i < this.cur_height; i++) {
            int cut_index = path[i];
            for (int j = 0; j < this.cur_width; j++) {
                if (j < cut_index - 1) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];

                    result_energy_map[i][j] = energy_map[i][j];
                } else if (j == cut_index - 1) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];
                    if (energy_map[i][j] > 10000 || energy_map[i][j] < 0) {
                        result_energy_map[i][j] = energy_map[i][j];
                    } else {
                        if (j == 0 || i == 0 || j == this.cur_width - 1 || i == this.cur_height - 1) {
                            result_energy_map[i][j] = 1000;
                        } else {
                            int rx = this.image[i][j + 2][0] - this.image[i][j - 1][0];
                            int gx = this.image[i][j + 2][1] - this.image[i][j - 1][1];
                            int bx = this.image[i][j + 2][2] - this.image[i][j - 1][2];

                            int ry = this.image[i + 1][j][0] - this.image[i - 1][j][0];
                            int gy = this.image[i + 1][j][1] - this.image[i - 1][j][1];
                            int by = this.image[i + 1][j][2] - this.image[i - 1][j][2];

                            result_energy_map[i][j] = Math.sqrt(rx * rx + gx * gx + bx * bx + ry * ry + gy * gy + by * by);
                        }
                    }
                } else if (j == cut_index + 1) {
                    result_matrix[i][j - 1][0] = this.image[i][j][0];
                    result_matrix[i][j - 1][1] = this.image[i][j][1];
                    result_matrix[i][j - 1][2] = this.image[i][j][2];
                    if (energy_map[i][j] > 10000 || energy_map[i][j] < 0) {
                        result_energy_map[i][j - 1] = energy_map[i][j];
                    } else {
                        if (j == 0 || i == 0 || j == this.cur_width - 1 || i == this.cur_height - 1) {
                            result_energy_map[i][j - 1] = 1000;
                        } else {
                            int rx = this.image[i][j + 1][0] - this.image[i][j - 2][0];
                            int gx = this.image[i][j + 1][1] - this.image[i][j - 2][1];
                            int bx = this.image[i][j + 1][2] - this.image[i][j - 2][2];

                            int ry = this.image[i + 1][j][0] - this.image[i - 1][j][0];
                            int gy = this.image[i + 1][j][1] - this.image[i - 1][j][1];
                            int by = this.image[i + 1][j][2] - this.image[i - 1][j][2];

                            result_energy_map[i][j - 1] = Math.sqrt(rx * rx + gx * gx + bx * bx + ry * ry + gy * gy + by * by);
                        }
                    }
                } else if (j > cut_index + 1) {
                    result_matrix[i][j - 1][0] = this.image[i][j][0];
                    result_matrix[i][j - 1][1] = this.image[i][j][1];
                    result_matrix[i][j - 1][2] = this.image[i][j][2];

                    result_energy_map[i][j - 1] = energy_map[i][j];
                }
            }
        }
        this.cur_width = this.cur_width - 1;
        this.image = result_matrix;
        return result_energy_map;
    }

    //一次性扩张图片函数
    private boolean expand_horizontal(double[][] accumulated_map) {
        if (this.res_height <= this.cur_height) {
            System.out.println("In expand_horizontal function, the size of the image does not need to be expanded");
            return false;
        }
        //找出能量只占百分之十以内的裂缝数
        //获取总能量并且构建将能量图的最后一列构建成一个新的数组
        double total_energy = 0.0;
        int last_col = this.cur_width - 1;
        double[] accumulated_last_col = new double[this.cur_height];
        for (int i = 0; i < this.cur_height; i++) {
            double tmp = accumulated_map[i][last_col];
            total_energy += tmp;
            accumulated_last_col[i] = tmp;
        }
        //给这个数组排序，从小的到大
        Arrays.sort(accumulated_last_col);
        //获取只占总能量百分之十以内的裂缝
        ArrayList<Integer> indices = new ArrayList<Integer>();
        double accumulated_energy = 0.0;
        for (int i = 0; i < this.cur_height; i++) {
            accumulated_energy += accumulated_last_col[i];
            indices.add(i);
            if (accumulated_energy > total_energy * 0.3) {
                break;
            }
        }

        int num_expand = this.res_height - this.cur_height;
        if (indices.size() >= num_expand) {
            for (int i = 0; i < num_expand; i++) {
                int[] path = find_min_energy_path_horizontal(accumulated_map, indices.get(i));
                //修正path
                for (int j = 0; j < i; j++) {
                    if (indices.get(i) > indices.get(j)) {
                        for (int k = 0; k < path.length; k++) {
                            path[k]++;
                        }
                    }
                }
                //扩张图片
                this.image = expand_1step_horizontal(path);
            }
        } else {
            int[] already_expand = new int[indices.size() - 1]; //储存从index从1到indices.size()-1的扩张次数
            for (int i = 0; i <= indices.size() - 2; i++) {
                int tmp_expand = (int) (accumulated_last_col[i] / (total_energy * 0.3) * num_expand);
                already_expand[i] = tmp_expand;
                int[] path = find_min_energy_path_horizontal(accumulated_map, indices.get(indices.size() - 1 - i));
                //修正path
                for (int j = 0; j < i; j++) {
                    if (indices.get(indices.size() - 1 - i) > indices.get(indices.size() - 1 - j)) {
                        for (int k = 0; k < path.length; k++) {
                            path[k] += already_expand[j];
                        }
                    }
                }
                //扩张图片
                for (int j = 0; j < tmp_expand; j++) {
                    this.image = expand_1step_horizontal(path);
                }
            }
            int[] path = find_min_energy_path_horizontal(accumulated_map, indices.getFirst());
            //修正path
            for (int j = 0; j < indices.size() - 1; j++) {
                if (indices.getFirst() > indices.get(indices.size() - 1 - j)) {
                    for (int k = 0; k < path.length; k++) {
                        path[k] += already_expand[j];
                    }
                }
            }
            int total_already_expand = 0;
            for (int i = 0; i < indices.size() - 1; i++) {
                total_already_expand += already_expand[i];
            }
            //扩展图片
            for (int i = 0; i < num_expand - total_already_expand; i++) {
                this.image = expand_1step_horizontal(path);
            }
        }
        return true;
    }

    //横向扩张一步函数
    public int[][][] expand_1step_horizontal(int[] path) {
        int[][][] result_matrix = new int[this.cur_height + 1][this.cur_width][3];
        for (int j = 0; j < this.cur_width; j++) {
            int expand_index = path[j];
            for (int i = 0; i < this.cur_height; i++) {
                if (i < expand_index) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];
                } else if (i == expand_index) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    ;
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];

                    result_matrix[i + 1][j][0] = this.image[i][j][0];
                    result_matrix[i + 1][j][1] = this.image[i][j][1];
                    result_matrix[i + 1][j][2] = this.image[i][j][2];
                } else {
                    result_matrix[i + 1][j][0] = this.image[i][j][0];
                    result_matrix[i + 1][j][1] = this.image[i][j][1];
                    result_matrix[i + 1][j][2] = this.image[i][j][2];
                }
            }
        }
        this.cur_height++;
        return result_matrix;
    }

    private boolean expand_vertical(double[][] accumulated_map) {
        if (this.res_width <= this.cur_width) {
            System.out.println("In expand_vertical function, the size of the image does not need to be expanded");
            return false;
        }
        //找出能量只占百分之十以内的裂缝数
        //获取总能量并且构建将能量图的最后一列构建成一个新的数组
        double total_energy = 0.0;
        int last_row = this.cur_height - 1;
        double[] accumulated_last_row = new double[this.cur_width];
        for (int j = 0; j < this.cur_width; j++) {
            double tmp = accumulated_map[last_row][j];
            total_energy += tmp;
            accumulated_last_row[j] = tmp;
        }
        //给这个数组排序
        Arrays.sort(accumulated_last_row);
        //获取只占总能量百分之十以内的裂缝
        ArrayList<Integer> indices = new ArrayList<Integer>();
        double accumulated_energy = 0.0;
        for (int j = 0; j < this.cur_width; j++) {
            accumulated_energy += accumulated_last_row[j];
            indices.add(j);
            if (accumulated_energy > total_energy * 0.3) {
                break;
            }
        }

        int num_expand = this.res_width - this.cur_width;
        if (indices.size() >= num_expand) {
            for (int i = 0; i < num_expand; i++) {
                int[] path = find_min_energy_path_vertical(accumulated_map, indices.get(i));
                //修正path
                for (int j = 0; j < i; j++) {
                    if (indices.get(i) > indices.get(j)) {
                        for (int k = 0; k < path.length; k++) {
                            path[k]++;
                        }
                    }
                }
                //扩张图片
                this.image = expand_1step_vertical(path);
            }
        } else {
            int[] already_expand = new int[indices.size() - 1]; //储存从index从1到indices.size()-1的扩张次数
            for (int i = 0; i <= indices.size() - 2; i++) {
                int tmp_expand = (int) (accumulated_last_row[i] / (total_energy * 0.3) * num_expand);
                already_expand[i] = tmp_expand;
                int[] path = find_min_energy_path_vertical(accumulated_map, indices.get(indices.size() - 1 - i));
                //修正path
                for (int j = 0; j < i; j++) {
                    if (indices.get(indices.size() - 1 - i) > indices.get(indices.size() - 1 - j)) {
                        for (int k = 0; k < path.length; k++) {
                            path[k] += already_expand[j];
                        }
                    }
                }
                //扩张图片
                for (int j = 0; j < tmp_expand; j++) {
                    this.image = expand_1step_vertical(path);
                }
            }
            int[] path = find_min_energy_path_vertical(accumulated_map, indices.getFirst());
            //修正path
            for (int j = 0; j < indices.size() - 1; j++) {
                if (indices.getFirst() > indices.get(indices.size() - 1 - j)) {
                    for (int k = 0; k < path.length; k++) {
                        path[k] += already_expand[j];
                    }
                }
            }
            int total_already_expand = 0;
            for (int i = 0; i < indices.size() - 1; i++) {
                total_already_expand += already_expand[i];
            }
            //扩展图片
            for (int i = 0; i < num_expand - total_already_expand; i++) {
                this.image = expand_1step_vertical(path);
            }
        }
        return true;
    }

    public int[][][] expand_1step_vertical(int[] path) {
        int[][][] result_matrix = new int[this.cur_height][this.cur_width + 1][3];
        for (int i = 0; i < this.cur_height; i++) {
            int expand_index = path[i];
            for (int j = 0; j < this.cur_width; j++) {
                if (j < expand_index) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];
                } else if (j == expand_index) {
                    result_matrix[i][j][0] = this.image[i][j][0];
                    result_matrix[i][j][1] = this.image[i][j][1];
                    result_matrix[i][j][2] = this.image[i][j][2];

                    result_matrix[i][j + 1][0] = this.image[i][j][0];
                    result_matrix[i][j + 1][1] = this.image[i][j][1];
                    result_matrix[i][j + 1][2] = this.image[i][j][2];
                } else {
                    result_matrix[i][j + 1][0] = this.image[i][j][0];
                    result_matrix[i][j + 1][1] = this.image[i][j][1];
                    result_matrix[i][j + 1][2] = this.image[i][j][2];
                }
            }
        }
        this.cur_width++;
        return result_matrix;
    }

    //一些中间函数
    //计算图像中单个点的能量
    private double energy_pixel(int y, int x) {
        double energy;
        if (x == 0 || y == 0 || x == this.cur_width - 1 || y == this.cur_height - 1) {
            energy = 1000;
        } else {
            int rx = this.image[y][x + 1][0] - this.image[y][x - 1][0];
            int gx = this.image[y][x + 1][1] - this.image[y][x - 1][1];
            int bx = this.image[y][x + 1][2] - this.image[y][x - 1][2];

            int ry = this.image[y + 1][x][0] - this.image[y - 1][x][0];
            int gy = this.image[y + 1][x][1] - this.image[y - 1][x][1];
            int by = this.image[y + 1][x][2] - this.image[y - 1][x][2];

            energy = Math.sqrt(rx * rx + gx * gx + bx * bx + ry * ry + gy * gy + by * by);
        }
        return energy;
    }

    //计算一整张图像中每个点的能量
    private double[][] energy_map() {
        double[][] map = new double[this.cur_height][this.cur_width];
        for (int i = 0; i < this.cur_height; i++) {
            for (int j = 0; j < this.cur_width; j++) {
                map[i][j] = energy_pixel(i, j);
            }
        }
        //保护
        if (this.protected_area != null) {
            for (int i = 0; i < this.protected_area.size(); i += 2) {
                map[this.protected_area.get(i + 1)][this.protected_area.get(i)] += 1000000;
            }
        }
        //建议删除
        if (this.removal_area != null) {
            for (int i = 0; i < this.removal_area.size(); i += 2) {
                map[this.removal_area.get(i + 1)][this.removal_area.get(i)] -= 1000000;
            }
        }
        return map;
    }

    //计算一整张图像中每一个点的累积能量,累积的方向是从左到右
    private double[][] accumulated_energy_map_horizontal(double[][] energy_map) {
        double[][] accumulated_map = new double[this.cur_height][this.cur_width];
        utils tool = new utils();

        //将第一列的能量直接存进去，因为没有上一行，故累积就是它们本身
        for (int i = 0; i < this.cur_height; i++)
            accumulated_map[i][0] = energy_map[i][0];

        for (int i = 0; i < this.cur_height; i++) {
            for (int j = 1; j < this.cur_width; j++) {
                if (i == 0)
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num2(accumulated_map[i][j - 1], accumulated_map[i + 1][j - 1]);
                else if (i == this.cur_height - 1)
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num2(accumulated_map[i - 1][j - 1], accumulated_map[i][j - 1]);
                else
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num3(accumulated_map[i - 1][j - 1], accumulated_map[i][j - 1], accumulated_map[i + 1][j - 1]);
            }
        }
        return accumulated_map;
    }

    //计算一整张图像中每一个点的累积能量，累积的方向是从上到下
    private double[][] accumulated_energy_map_vertical(double[][] energy_map) {
        double[][] accumulated_map = new double[this.cur_height][this.cur_width];
        utils tool = new utils();

        //将第一行的能量直接存进去，因为没有上一行，故累积就是它们本身
        for (int j = 0; j < this.cur_width; j++)
            accumulated_map[0][j] = energy_map[0][j];

        for (int i = 1; i < this.cur_height; i++) {
            for (int j = 0; j < this.cur_width; j++) {
                if (j == 0)
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num2(accumulated_map[i - 1][j], accumulated_map[i - 1][j + 1]);
                else if (j == this.cur_width - 1)
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num2(accumulated_map[i - 1][j - 1], accumulated_map[i - 1][j]);
                else
                    accumulated_map[i][j] = energy_map[i][j] + tool.find_min_num3(accumulated_map[i - 1][j - 1], accumulated_map[i - 1][j], accumulated_map[i - 1][j + 1]);
            }
        }
        return accumulated_map;
    }

    //根据accumulated_map追溯出最优的路径
    //路径储存在一维数组当中,储存的顺序是从第一列到最后一列
    private int[] find_min_energy_path_horizontal(double[][] accumulated_map, int start_index) {
        utils tool = new utils();
        int[] path = new int[this.cur_width];
        int index = start_index;

        path[this.cur_width - 1] = index;
        path[this.cur_width - 2] = index;
        for (int j = this.cur_width - 3; j >= 1; j--) {
            if (index == 0) {
                if (accumulated_map[index][j] > accumulated_map[index + 1][j])
                    index = index + 1;
            } else if (index + 1 == this.cur_height) {
                if (accumulated_map[index][j] > accumulated_map[index - 1][j])
                    index = index - 1;
            } else {
                index = index + tool.find_min_index3(accumulated_map[index - 1][j], accumulated_map[index][j], accumulated_map[index + 1][j]);
            }
            path[j] = index;
        }
        path[0] = path[1];
        return path;
    }

    //路径储存在一维数组当中,储存的顺序是从第一行到最后一行
    private int[] find_min_energy_path_vertical(double[][] accumulated_map, int start_index) {
        utils tool = new utils();
        int[] path = new int[this.cur_height];
        int index = start_index;

        path[this.cur_height - 1] = index;
        path[this.cur_height - 2] = index;
        for (int i = this.cur_height - 3; i >= 1; i--) {
            if (index == 0) {
                if (accumulated_map[i][index] > accumulated_map[i][index + 1])
                    index = index + 1;
            } else if (index + 1 == this.cur_width) {
                if (accumulated_map[i][index] > accumulated_map[i][index - 1])
                    index = index - 1;
            } else {
                index = index + tool.find_min_index3(accumulated_map[i][index - 1], accumulated_map[i][index], accumulated_map[i][index + 1]);
            }
            path[i] = index;
        }
        path[0] = path[1];
        return path;
    }
}