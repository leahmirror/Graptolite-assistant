#include "include/ocr.h"
//#include<opencv2\opencv.hpp>
#include<iostream>
//#define cout fout
ofstream fout("../../output.txt");
#include <string.h>
using namespace std;
using namespace cv;



vector<Mat> read_images_in_folder(vector<cv::String> fn);
string predict() {
    //if (argc != 2) {
        //fprintf(stderr, "Usage: %s [model model path imagepath \n", argv[0]);
        //return -1;
    //}
    //cout << argv[0] <<endl;
    //获取图片文件夹路径l
    //const char *imagepath = argv[1];
    //文件夹
    //vector<cv::String> fn;
    //glob(imagepath, fn, false);
    //vector<Mat> images = read_images_in_folder(fn);
    //std::vector<string> result_list;
    //遍历
    cv::Mat im_bgr = cv::imread("",0);
    //const int long_size = atoi(argv[2]);
    OCR *ocrengine = new OCR();
    //cout<<fn[i].substr()<<endl;
    //std::string filename = fn[i].c_str();
    //字符串切分
    //获取filename
    //string s = fn[i];
    string pattern = "/";
    //int index = s.find_last_of('/') + 1;
    string st = "";
    //cout<<st<<endl;
    string pre_result;
    //检测加识别
    pre_result = ocrengine -> detect(im_bgr);
    std::cout << "识别结果" << pre_result <<std::endl;
    //result_list.push_back(pre_result);
    //写进文件
    fout << st + ":" << pre_result  << endl;
    delete ocrengine;
        /*
    for(int i = 0; i < images.size();i++)
    {
        //cv::Mat im_bgr = cv::imread(imagepath, 1);
        cv::Mat im_bgr = images[i];
        //const int long_size = atoi(argv[2]);
        OCR *ocrengine = new OCR();
        //cout<<fn[i].substr()<<endl;
        //std::string filename = fn[i].c_str();
        //字符串切分
        //获取filename
        string s = fn[i];
        string pattern = "/";
        int index = s.find_last_of('/') + 1;
        string st = s.substr(index,s.length() - index);
        cout<<st<<endl;
        string pre_result;
        //检测加识别
        pre_result = ocrengine -> detect(im_bgr,st);
        std::cout << "识别结果" << pre_result <<std::endl;
        result_list.push_back(pre_result);
        //写进文件
        fout << st + ":" << result_list[i]  << endl;
        delete ocrengine;
    }
         */
    /*
    cout << "************************************************************" << endl;
    for (int i = 1; i <=result_list.size() ; ++i)
    {
        fout << result_list[i]  << endl;
    }
    cout << "************************************************************" << endl;
     */
    return pre_result;
}

//opencv 读取图片
/*
vector<Mat> read_images_in_folder( vector<cv::String> fn)
{
    vector<Mat> images;
    size_t count = fn.size(); //number of png files in images folder
    for (size_t i = 0; i < count; i++)
    {
        images.push_back(imread(fn[i]));//push_back()   在数组的最后添加一个数据
    }
    return images;
}
*/