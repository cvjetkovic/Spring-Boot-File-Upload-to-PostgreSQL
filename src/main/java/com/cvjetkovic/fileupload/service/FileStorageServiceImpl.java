package com.cvjetkovic.fileupload.service;

import com.cvjetkovic.fileupload.exception.FileStorageException;
import com.cvjetkovic.fileupload.exception.MyFileNotFoundException;
import com.cvjetkovic.fileupload.fileops.ByteArrayCompressDecompress;
import com.cvjetkovic.fileupload.model.FileModel;
import com.cvjetkovic.fileupload.model.PreviewFileModel;
import com.cvjetkovic.fileupload.payload.AllFilesResponse;
import com.cvjetkovic.fileupload.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Cvjetkovic
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private FileRepository fileRepository;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

    public AllFilesResponse listAllFiles() {
        List<FileModel> files = fileRepository.findAll();
        List<PreviewFileModel> returnValue = new ArrayList<>();
        AllFilesResponse allFilesResponse = new AllFilesResponse();

        for (FileModel fileFromRepo : files) {
            PreviewFileModel previewFileModel = new PreviewFileModel();
            previewFileModel.setId(fileFromRepo.getId());
            previewFileModel.setFileName(fileFromRepo.getFilename());
            previewFileModel.setFileType(fileFromRepo.getFileType());
            previewFileModel.setTimestamp(fileFromRepo.getTimestamp());
            previewFileModel.setSize(fileFromRepo.getData().length);
            returnValue.add(previewFileModel);
        }
        allFilesResponse.setAllFiles(returnValue);
        return allFilesResponse;
    }

    public FileModel storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ByteArrayCompressDecompress byteArrayCompressDecompress = new ByteArrayCompressDecompress();

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            String name = fileName.split("\\.")[0];
            FileModel fileModel = new FileModel(name, file.getContentType(), file.getBytes(), timestamp);

            System.out.println("Filesize normal: " + file.getBytes().length);
            byte compressed[] = byteArrayCompressDecompress.compressByteArray(file.getBytes());
            System.out.println("Filesize compressed" + compressed.length);
            byte decompressed[] = byteArrayCompressDecompress.decompressByteArray(compressed);
            System.out.println("Filesize decompressed" + decompressed.length);



            return fileRepository.save(fileModel);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public FileModel getFile(String fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new MyFileNotFoundException("File not found with id " + fileId));
    }


    public FileModel deleteFile(String lawId) {
        fileRepository.deleteById(lawId);
        return null;
    }


}