#include "include/ocr.h"
#include <queue>
#include <iostream>
#include <stdio.h>
#include "opencv2/core/core.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <iostream>
#include <string>
#define indef_min -32767
#define CRNN_LSTM 0


OCR::OCR()
{
    //ncnn::create_gpu_instance();
    //psenet.opt.use_vulkan_compute = true;
    //psenet.opt.use_packing_layout = true;
  //  psenet.load_param("../assets/lite.param");
  //  psenet.load_model("../assets/lite.bin");
    //psenet.load_param("../../models/pse_v3.param");
    //psenet.load_model("../../models/pse_v3.bin");

    //psenet.load_param("../../models/psenet_lite_mbv2.param");
    //psenet.load_model("../../models/psenet_lite_mbv2.bin");

#if CRNN_LSTM
    crnn_net.load_param("../../models/crnn_lite_lstm_v2.param");
    crnn_net.load_model("../../models/crnn_lite_lstm_v2.bin");
    //crnn_vertical_net.load_param("../../models/crnn_lite_lstm_vertical.param");
    //crnn_vertical_net.load_model("../../models/crnn_lite_lstm_vertical.bin");
#else
  /*  crnn_net.load_param("../../models/crnn_lite_dw_dense.param");
    crnn_net.load_model("../../models/crnn_lite_dw_dense.bin");
    crnn_vertical_net.load_param("../../models/crnn_lite_dw_dense_vertical.param");
    crnn_vertical_net.load_model("../../models/crnn_lite_dw_dense_vertical.bin");*/
#endif
    //angle_net.load_param("../../models/shufflenetv2_05_angle.param");
    //angle_net.load_model("../../models/shufflenetv2_05_angle.bin");

    //load keys
    ifstream in("../assets/keys.txt");
	std::string filename;
	std::string line;

	if(in) // 有该文件
	{
		while (getline (in, line)) // line中不包括每行的换行符
		{
            alphabetChinese.push_back(line);
		}
	}
	else // 没有该文件
	{
		std::cout <<"no txt file" << std::endl;
	}

}

//判断字符串是不是数字
bool isNum(string str)
{
    stringstream sin(str);
    double d;
    char c;
    if(!(sin >> d))
        return false;
    if (sin >> c)
        return false;
    return true;
}

std::vector<std::string> crnn_deocde(const ncnn::Mat score , std::vector<std::string> alphabetChinese) {
    float *srcdata = (float* ) score.data;
    std::vector<std::string> str_res;
    int last_index = 0;  
    for (int i = 0; i < score.h;i++){
        int max_index = 0;
        
        float max_value = -1000;
        for (int j =0; j< score.w; j++){
            if (srcdata[ i * score.w + j ] > max_value){
                max_value = srcdata[i * score.w + j ];
                max_index = j;
            }
        }
        if (max_index >0 && (not (i>0 && max_index == last_index))  ){
//            std::cout <<  max_index - 1 << std::endl;
//            std::string temp_str =  utf8_substr2(alphabetChinese,max_index - 1,1)  ;
            str_res.push_back(alphabetChinese[max_index-1]);
        }



        last_index = max_index;
    }
    return str_res;
}

/*
 *
 */

void visualize(const char* title, const ncnn::Mat& m)
{
    std::vector<cv::Mat> normed_feats(m.c);

    for (int i=0; i<m.c; i++)
    {
        cv::Mat tmp(m.h, m.w, CV_32FC1, (void*)(const float*)m.channel(i));

        cv::normalize(tmp, normed_feats[i], 0, 255, cv::NORM_MINMAX, CV_8U);

        cv::cvtColor(normed_feats[i], normed_feats[i], cv::COLOR_GRAY2BGR);

        // check NaN
        for (int y=0; y<m.h; y++)
        {
            const float* tp = tmp.ptr<float>(y);
            uchar* sp = normed_feats[i].ptr<uchar>(y);
            for (int x=0; x<m.w; x++)
            {
                float v = tp[x];
                if (v != v)
                {
                    sp[0] = 0;
                    sp[1] = 0;
                    sp[2] = 255;
                }

                sp += 3;
            }
        }
    }

    int tw = m.w < 10 ? 32 : m.w < 20 ? 16 : m.w < 40 ? 8 : m.w < 80 ? 4 : m.w < 160 ? 2 : 1;
    int th = (m.c - 1) / tw + 1;

    cv::Mat show_map(m.h * th, m.w * tw, CV_8UC3);
    show_map = cv::Scalar(127);

    // tile
    for (int i=0; i<m.c; i++)
    {
        int ty = i / tw;
        int tx = i % tw;

        normed_feats[i].copyTo(show_map(cv::Rect(tx * m.w, ty * m.h, m.w, m.h)));
    }

    cv::resize(show_map, show_map, cv::Size(0,0), 2, 2, cv::INTER_NEAREST);
    //cv::imwrite("../../aaa.jpg", show_map);
    //cv::imshow(title, show_map);
}


cv::Mat resize_img(cv::Mat src)
{
    int w = src.cols;
    int h = src.rows;
    // std::cout<<"原图尺寸 (" << w << ", "<<h<<")"<<std::endl;
    float scale = 1.f;
    // std::cout<<"缩放尺寸 (" << w << ", "<<h<<")"<<std::endl;
    cv::Mat result;
    //w, h = im.rows,im.cols
    int size = 224;
    //int ow = 0;
    //int oh = 0;
    if ((w <= h and w == size) || (h <= w and h == size))
    {
        int ow = w;
        int oh = h;
        cv::resize(src, result, cv::Size(ow, oh));
    }
    if (w < h)
    {
        int ow = size;
        int oh = int(size * h / w);
        cv::resize(src, result, cv::Size(ow, oh));
    }
    else{
        int oh = size;
        int ow = int(size * w / h);
        cv::resize(src, result, cv::Size(ow, oh));
    }

    //return img.resize((ow, oh), interpolation)
    //cv::cvtColor(src,result,CV_BGR2GRAY);

    const int cropSize = 224;
    const int offsetW = (result.cols - cropSize) / 2;
    const int offsetH = (result.rows - cropSize) / 2;
    //const cv::Rect roi(offsetW, offsetH, cropSize, cropSize);
    cv::Rect roi = cv::Rect(offsetW, offsetH, cropSize, cropSize);
    result = result(roi).clone();
    return result;
}
//排序顺时针排序四点坐标
//author:hengherui
/*
 * time:2020/05/29
 * fuction:sort the wisecolock points
 */

cv::Mat draw_bbox(cv::Mat &src, const std::vector<std::vector<cv::Point>> &bboxs) {
    cv::Mat dst;
    if (src.channels() == 1) {
        cv::cvtColor(src, dst, cv::COLOR_GRAY2BGR);
    } else {
        dst = src.clone();
    }
    auto color = cv::Scalar(0, 0, 255);
    std::vector<cv::Point> points;
    for (auto bbox :bboxs) {

        cv::line(dst, bbox[0], bbox[1], color, 3);
        cv::line(dst, bbox[1], bbox[2], color, 3);
        cv::line(dst, bbox[2], bbox[3], color, 3);
        cv::line(dst, bbox[3], bbox[0], color, 3);
        //points = getclockwisePoints(bbox);


    }
    return dst;
}



void pse_deocde(ncnn::Mat& features,
                              std::map<int, std::vector<cv::Point>>& contours_map,
                              const float thresh,
                              const float min_area,
                              const float ratio
                              )
{

        /// get kernels
        float *srcdata = (float *) features.data;
        std::vector<cv::Mat> kernels;

        float _thresh = thresh;
        cv::Mat scores = cv::Mat::zeros(features.h, features.w, CV_32FC1);
        for (int c = features.c - 1; c >= 0; --c){
            cv::Mat kernel(features.h, features.w, CV_8UC1);
            for (int i = 0; i < features.h; i++) {
                for (int j = 0; j < features.w; j++) {

                    if (c==features.c - 1) scores.at<float>(i, j) = srcdata[i * features.w + j + features.w*features.h*c ] ;

                    if (srcdata[i * features.w + j + features.w*features.h*c ] >= _thresh) {
                    // std::cout << srcdata[i * src.w + j] << std::endl;
                        kernel.at<uint8_t>(i, j) = 1;
                    } else {
                        kernel.at<uint8_t>(i, j) = 0;
                        }

                }
            }
            kernels.push_back(kernel);
            _thresh = thresh * ratio;
        }
        /// make label
        cv::Mat label;
        std::map<int, int> areas;
        std::map<int, float> scores_sum;
        cv::Mat mask(features.h, features.w, CV_32S, cv::Scalar(0));
        cv::connectedComponents(kernels[features.c  - 1], label, 4);




        for (int y = 0; y < label.rows; ++y) {
            for (int x = 0; x < label.cols; ++x) {
                int value = label.at<int32_t>(y, x);
                float score = scores.at<float>(y,x);
                if (value == 0) continue;
                areas[value] += 1;

                scores_sum[value] += score;
            }
        }

        std::queue<cv::Point> queue, next_queue;

        for (int y = 0; y < label.rows; ++y) {

            for (int x = 0; x < label.cols; ++x) {
                int value = label.at<int>(y, x);

                if (value == 0) continue;
                if (areas[value] < min_area) {
                    areas.erase(value);
                    continue;
                }

                if (scores_sum[value]*1.0 /areas[value] < 0.93  )
                {
                    areas.erase(value);
                    scores_sum.erase(value);
                    continue;
                }
                cv::Point point(x, y);
                queue.push(point);
                mask.at<int32_t>(y, x) = value;
            }
        }

        /// growing text line
        int dx[] = {-1, 1, 0, 0};
        int dy[] = {0, 0, -1, 1};

        for (int idx = features.c  - 2; idx >= 0; --idx) {
            while (!queue.empty()) {
                cv::Point point = queue.front(); queue.pop();
                int x = point.x;
                int y = point.y;
                int value = mask.at<int32_t>(y, x);

                bool is_edge = true;
                for (int d = 0; d < 4; ++d) {
                    int _x = x + dx[d];
                    int _y = y + dy[d];

                    if (_y < 0 || _y >= mask.rows) continue;
                    if (_x < 0 || _x >= mask.cols) continue;
                    if (kernels[idx].at<uint8_t>(_y, _x) == 0) continue;
                    if (mask.at<int32_t>(_y, _x) > 0) continue;

                    cv::Point point_dxy(_x, _y);
                    queue.push(point_dxy);

                    mask.at<int32_t>(_y, _x) = value;
                    is_edge = false;
                }

                if (is_edge) next_queue.push(point);
            }
            std::swap(queue, next_queue);
        }

        /// make contoursMap
        for (int y=0; y < mask.rows; ++y)
            for (int x=0; x < mask.cols; ++x) {
                int idx = mask.at<int32_t>(y, x);
                if (idx == 0) continue;
                contours_map[idx].emplace_back(cv::Point(x, y));
            }
}





cv::Mat matRotateClockWise180(cv::Mat src)//顺时针180
{
	//0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
	flip(src, src, 0);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
	flip(src, src, 1);
	return src;
	//transpose(src, src);// 矩阵转置
}

cv::Mat matRotateClockWise90(cv::Mat src)
{

	// 矩阵转置
	transpose(src, src);
	//0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
	flip(src, src, 1);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）
	return src;
}
/*
 *
 */
void ncnn_debug(ncnn::Mat& ncnn_img, string img_name)
{
    cv::Mat imageDate(ncnn_img.h, ncnn_img.w, CV_8UC3);
    for (int c = 0; c < 3; c++) {
        for (int i = 0; i < ncnn_img.h; i++)
        {
            for (int j = 0; j < ncnn_img.w; j++)
            {
                float t = ((float*)ncnn_img.data)[j + i * ncnn_img.w + c * ncnn_img.h * ncnn_img.w];
                imageDate.data[(2 - c) + j * 3 + i * ncnn_img.w * 3] = t;
            }
        }
    }
//    cv::imwrite("../../" + img_name, imageDate);
}

string  OCR::detect(cv::Mat im_bgr)
{
// 图像缩放
    auto im = resize_img(im_bgr);
    //cv::Rect rect;
    //cv::Mat im = img.clone();


    float h_scale = im_bgr.rows * 1.0 / im.rows;
    float w_scale = im_bgr.cols * 1.0 / im.cols;
    //float h_scale = im.rows /  im_bgr.rows * 1.0 ;
    //float w_scale = h_scale ;
    ncnn::Mat in = ncnn::Mat::from_pixels(im.data, ncnn::Mat::PIXEL_BGR, im.cols, im.rows);
    in.substract_mean_normalize(mean_vals_pse_angle,norm_vals_pse_angle);

    std::cout << "输入尺寸 (" << in.w << ", " << in.h << ")" << std::endl;

    ncnn::Extractor ex = psenet.create_extractor();
    ex.set_num_threads(num_thread);
    ex.input("input", in);
    ncnn::Mat preds;
    double time1 = static_cast<double>( cv::getTickCount());
    ex.extract("output", preds);
    std::cout << "psenet前向时间:" << (static_cast<double>( cv::getTickCount()) - time1) / cv::getTickFrequency() << "s" << std::endl;
    std::cout << "网络输出尺寸 (" << preds.data  << std::endl;
    std::vector<float> scores;
    scores.resize(preds.w);
    for (int j=0; j<preds.w; j++)
    {
        scores[j] =preds[j];
    }

    std::vector<float>::iterator biggest = std::max_element(scores.begin(), scores.end()); //iterator
    float first = *biggest;
    auto position = std::distance(scores.begin(), biggest);
    std::cout << "Max element is " << *biggest << " at position " << std::distance(scores.begin(), biggest) << std::endl;

    std::map<long,string> class_map;
    class_map[0] = "Agetograptus primus原始精妙笔石（LM3）";class_map[1] = "Akidograptus ascensus尖削尖笔石（时代LM2-LM3）";
    class_map[2] = "Amplexogratus latus宽型围笔石(WF1-4)";class_map[3] = "Anticostia tenuissima细茎安提可斯笔石（时代WF3）";
    class_map[4] = "Appendispinograptus supernus高层附刺笔石（时代WF3-WF4）";class_map[5] = "Campograptus communis通常弯曲笔石（LM6）";
    class_map[6] = "Climacograptus tubuliferus管状栅笔石（时代WF3-WF4）";class_map[7] = "Coronograptus cyphus曲背冠笔石（时代LM5）";
    class_map[8] = "Cystograptus vesiculosus轴囊囊笔石（时代LM4）";class_map[9] = "Demirastrites triangulatus三角半耙笔石（时代LM6）";
    class_map[10] = "Dicellograptus complexus环绕叉笔石（时代WF2）";class_map[11] = "Dimorphograptus两形笔石属 (LM3-LM5)";
    class_map[12] = "Neodiplograptus新双笔石属（时代LM1-LM3）";class_map[13] = "Normalograptus persculptus雕刻正常笔石（时代LM1）";
    class_map[14] = "Null";class_map[15] = "OKtavites spiralis奥氏螺旋笔石（LM9）";
    class_map[16] = "Orthograptus socilis骨条肋直笔石 （时代WF3）";class_map[17] = "Parakidograptus acuminatus尖削拟尖笔石（时代LM3）";
    class_map[18] = "Paraorthograptus pacificus太平洋拟直笔石（时代WF3-WF4）";class_map[19] = "Paraplegmatograptus拟绞笔石属（WF3-WF4)";
    class_map[20] = "Petalolithus folium叶状花瓣笔石（LM6-LM7）";class_map[21] = "Pristiograptus regularis规则锯笔石（LM7-LM9）";
    class_map[22] = "Pseudorthorgraptus inopinatus意外假直笔石(LM6-LM7)";class_map[23] = "Rastrites orbitus环形耙笔石（LM6-LM9）";
    class_map[24] = "Rectograptus abbreviatus缩短直管笔石（时代WF1-WF4）";class_map[25] = "Rectograptus obesas肥大直管笔石(WF2-WF3)";
    class_map[26] = "Tangyagraptus typicus典型棠垭笔石（时代WF3b）";class_map[27] = "coronograptus gregarius群集冠笔石(LM6)";
    class_map[28] = "pribylograptus incommodus碎屑普利贝笔石（LM4-LM6）";
    auto it = class_map.find(position);
    //iter = m.find(key);
    string pre ="";
    if(class_map.count(position)>0)
    {
        pre = class_map[position];
    }
    scores[position] = indef_min;

    string pre2 = "";
    std::vector<float>::iterator biggest2 = std::max_element(scores.begin(), scores.end()); //iterator
    float second = *biggest2;
    auto position2 = std::distance(scores.begin(), biggest2);
    std::cout << "Max element is " << *biggest2 << " at position " << std::distance(scores.begin(), biggest2) << std::endl;
    if(class_map.count(position2)>0)
    {
        pre2 = class_map[position2];
    }

    scores[position2] = indef_min;

    string pre3 = "";
    std::vector<float>::iterator biggest3 = std::max_element(scores.begin(), scores.end()); //iterator
    float third = *biggest3;
    auto position3 = std::distance(scores.begin(), biggest3);
    std::cout << "Max element is " << *biggest3 << " at position " << std::distance(scores.begin(), biggest3) << std::endl;
    if(class_map.count(position3)>0)
    {
        pre3 = class_map[position3];
    }

    //string pre_f = pre + " " + pre2 + " " + pre3;
    //std::vector<double>::iterator smallest = std::min_element(vec.begin(), vec.end());
    //std::cout << "Min element is " << *smallest << " at position " << std::distance(vec.begin(), smallest) << std::endl;
    //ncnn_debug(preds,"ceshi.jpg");
    //cv::Mat cv_mat = cv::Mat::zeros(cv::Size(100, 100), CV_8UC1);
    //ncnn::Mat ppp;
    //转化为opencv的Mat进行操作，因为有很多矩阵运算就很方便
    //visualize("pic",preds);
    //preds.to_pixels(cv_mat.data, ncnn::Mat::PIXEL_RGB);
    //cv::imwrite("../../ceshi111111111.jpg", cv_mat);
    ///cv::Mat cv_img = cv::Mat::zeros(rows,cols,opencv_typ);
    //ncnn::Mat::to_pixels(cv_img.data,ncnn::Mat::PIXEL_RGB)
    //std::cout << first<<second << third<<std::endl;
    //float total =  fabsf(first) + fabsf(second) + fabsf(third);
    //float rat1 = fabsf(first) / total * 100;
    //float rat2 = fabsf(second) / total * 100;
    //float rat3 = fabsf(third) / total * 100;
    //char rat_1[20], rat_2[20], rat_3[20];
    //sprintf(rat_1, "%.2f", rat1);
    //sprintf(rat_2, "%.2f", rat2);
    //sprintf(rat_3, "%.2f", rat3);
    string pre_f = pre + "  "+ pre2 + "  " + pre3 ;
    return pre_f;
}

int OCR::init(AAssetManager *pManager, string basicString, string basicString1) {
    int ret1 =psenet.load_param(pManager, basicString.c_str());
    int ret2 =psenet.load_model(pManager, basicString1.c_str());
    string path="";
    AAsset *asset=AAssetManager_open(pManager,(path+"keys.txt").c_str(),AASSET_MODE_BUFFER);
    if(!asset){return 0;}
    unsigned long len=(unsigned long)AAsset_getLength(asset);
    std::string words;
    words.resize(len);
    int ret=AAsset_read(asset,(void *)words.data(),len);
    AAsset_close(asset);
    if(ret!=len){
        return 0;
    }
    std::istringstream f(words);
    std::string line;
    while (getline(f,line)){alphabetChinese.push_back(line);}
    return (ret1||ret2);
}
