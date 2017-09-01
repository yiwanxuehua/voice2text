package com.iflytek.msp.lfasr;

import java.util.HashMap;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSON;
import com.iflytek.msp.cpdb.lfasr.client.LfasrClientImp;
import com.iflytek.msp.cpdb.lfasr.exception.LfasrException;
import com.iflytek.msp.cpdb.lfasr.model.LfasrType;
import com.iflytek.msp.cpdb.lfasr.model.Message;
import com.iflytek.msp.cpdb.lfasr.model.ProgressStatus;

// SDK document: http://www.xfyun.cn/doccenter/lfasr#go_sdk_doc_v2
public class TestLfasr 
{
	// original media path
	private static final String local_file = "...";

	private static final LfasrType type = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;
	
	private static int sleepSecond = 20;
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
	
		LfasrClientImp lc = null;
		try {
			lc = LfasrClientImp.initLfasrClient();
		} catch (LfasrException e) {
			Message initMsg = JSON.parseObject(e.getMessage(), Message.class);
			System.out.println("ecode=" + initMsg.getErr_no());
			System.out.println("failed=" + initMsg.getFailed());
		}
				
		// get upload task id
		String task_id = "";
		HashMap<String, String> params = new HashMap<>();
		params.put("has_participle", "true");
		try {
			// upload media file to Server���Ƶ�ļ�
			Message uploadMsg = lc.lfasrUpload(local_file, type, params);ֵ
			int ok = uploadMsg.getOk();
			if (ok == 0) {
				task_id = uploadMsg.getData();
				System.out.println("task_id=" + task_id);
			} else {
				System.out.println("ecode=" + uploadMsg.getErr_no());
				System.out.println("failed=" + uploadMsg.getFailed());
			}
		} catch (LfasrException e) {
			Message uploadMsg = JSON.parseObject(e.getMessage(), Message.class);
			System.out.println("ecode=" + uploadMsg.getErr_no());
			System.out.println("failed=" + uploadMsg.getFailed());					
		}
		while (true) {
			try {
				Thread.sleep(sleepSecond * 1000);
				System.out.println("waiting ...");
			} catch (InterruptedException e) {
			}
			try {
				Message progressMsg = lc.lfasrGetProgress(task_id);
				if (progressMsg.getOk() != 0) {
					System.out.println("task was fail. task_id:" + task_id);
					System.out.println("ecode=" + progressMsg.getErr_no());
					System.out.println("failed=" + progressMsg.getFailed());
					continue;
				} else {
					ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
					if (progressStatus.getStatus() == 9) {
						// �������
						System.out.println("task was completed. task_id:" + task_id);
						break;	
					} else {
						System.out.println("task was incomplete. task_id:" + task_id + ", status:" + progressStatus.getDesc());
						continue;
					}
				}
			} catch (LfasrException e) {
				Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
				System.out.println("ecode=" + progressMsg.getErr_no());
				System.out.println("failed=" + progressMsg.getFailed());
			}
		}

		// ��ȡ������
		try {
			Message resultMsg = lc.lfasrGetResult(task_id);
			System.out.println(resultMsg.getData());
			if (resultMsg.getOk() == 0) {
				System.out.println(resultMsg.getData());
			} else {
				System.out.println("ecode=" + resultMsg.getErr_no());
				System.out.println("failed=" + resultMsg.getFailed());
			}
		} catch (LfasrException e) {
			// ��ȡ����쳣���������쳣������Ϣ
			Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
			System.out.println("ecode=" + resultMsg.getErr_no());
			System.out.println("failed=" + resultMsg.getFailed());
		}
	}
}
