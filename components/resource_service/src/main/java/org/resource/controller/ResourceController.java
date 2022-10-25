package org.resource.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class ResourceController {

   private static final String MEDIA_TYPE = "audio/mpeg";

   @Autowired
   private ResourceService resourceService;

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 400, message = "Validation error or request body is an invalid MP3"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @PostMapping(path = "/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public Long uploadNewResource(@RequestPart MultipartFile data) throws IOException {
      if(data.getContentType() == null || MEDIA_TYPE.compareToIgnoreCase(data.getContentType()) !=0 )
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation error or request body is an invalid MP3");
      return resourceService.uploadNewResource(data);

   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 206, message = "Partial content (if range requested)"),
               @ApiResponse(code = 404, message = "Resource doesn’t exist with given id"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @GetMapping(path = "/resources/{id}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<byte[]> getAudioBinaryData(@PathVariable Long id, @RequestParam(required = false, defaultValue = "") List<Integer> range) {
      if(range != null && !range.isEmpty())
         return getAudioBinaryDataWithRange(id, range);

      try {
         return new ResponseEntity<>(resourceService.getAudioBinaryData(id).readAllBytes(), HttpStatus.OK);
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error occurred.");
      }
   }

   @ApiResponses(
         value = {
               @ApiResponse(code = 200, message = "OK"),
               @ApiResponse(code = 500, message = "Internal server error occurred.")
         })
   @DeleteMapping(path = "/resources", produces = "application/json")
   public List<Long> deleteSongs(@RequestParam List<Long> id) {
      return resourceService.deleteSongs(id);
   }

   private ResponseEntity<byte[]> getAudioBinaryDataWithRange(Long id, List<Integer> range) {
      if(range.size() != 2)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      int length = range.get(1)-range.get(0);
      if(length < 0)
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid range");

      byte[] result = new byte[length];
      S3ObjectInputStream audioBinaryData = resourceService.getAudioBinaryData(id);

      try {
         audioBinaryData.skip(range.get(0));
         audioBinaryData.readNBytes(result, 0, length);
      } catch (IOException e) {
         throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid range");
      }

      return new ResponseEntity<>(result, HttpStatus.PARTIAL_CONTENT);
   }
}
