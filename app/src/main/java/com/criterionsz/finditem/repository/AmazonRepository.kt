package com.criterionsz.finditem.repository

import android.util.Log
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.DetectLabelsRequest
import com.amazonaws.services.rekognition.model.DetectLabelsResult
import com.amazonaws.services.rekognition.model.Image
import com.criterionsz.finditem.BuildConfig
import java.nio.ByteBuffer

class AmazonRepository {
    private val credentials = object : AWSCredentials {
        override fun getAWSAccessKeyId(): String {
            return BuildConfig.AWS_ACCESS_KEY_ID
        }

        override fun getAWSSecretKey(): String {
            return BuildConfig.AWS_SECRET_KEY
        }
    }
    private val rekognitionClient = AmazonRekognitionClient(
        credentials
    )

    fun getResult(tempBuffer: ByteBuffer): List<String> {
        val request = DetectLabelsRequest().withImage(
            Image().withBytes(
                tempBuffer
            )
        ).withMaxLabels(10)
        val result: DetectLabelsResult = rekognitionClient.detectLabels(request)
        val res = result.labels.map { it.name }
        Log.v("SecondFragment", res.joinToString(","))
        return  res
    }
}