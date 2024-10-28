# 后端的实现

本项目主要功能是，在影响最小的情况下，对图片进行适当的扩张或者裁剪（resize）。图像在这个项目中以三维的int数组进行储存，第一个维度为行，第二个维度为列，第三个维度为rgb。

## 文件

### seam_carving_rgb.java

#### 总述

该文件中实现类 seam_carving_rgb ，其函数 resize() 实现项目的主要功能。

**seam_carving_rgb**

- 类成员
    - image 原始图像
    - cur_height 原始图像的高，对应三维矩阵中的第一维
    - cur_width 原始图像的宽，对应三维矩阵中的第二维
    - res_height 裁剪后图像的高
    - res_width 裁剪后图像的宽
    - protected_area 保护区域
    - removal_area 建议删除区域
- 构造器
    - 输入：
        - origin_image 原始图像
- resize()
    - 输入：
        - height 裁剪后图像的高
        - width 裁剪后图像的宽
        - protected_area 保护区域
        - removal_area 建议删除区域
    - 输出：
        - res_image 裁剪后的图像
- cut_1step_horizontal()
    - 输入：
        - path 删除的路径
        - energy_map 能量矩阵
        - 输出：
            - result_energy_map 删除一条路径后的能量矩阵
    - cut_1step_vertical()
        - 与cut_1step_horizontal()相似
- expand_horizontal()
    - 输入：
        - accumulated_map 累积能量矩阵
    - 输出：
        - true
- expand_vertical()
    - 与expand_horizontal()类似

### utils.java

#### 总述

汇总了一些被重复调用的函数，或者类。主要实现了 bonding_box 类。

**bonding_box**

- 类成员
    - x1 bonding_box左上角点的横坐标
    - y1 bonding_box左上角点的纵坐标
    - x2 bonding_box右下角点的横坐标
    - y2 bonding_box右下角点点纵坐标
- info()
    - 输入：
        - 无
    - 输出：
        - box 大小为4的1维数组，分别储存[x1, y1, x2, y2]
