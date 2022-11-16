package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "songsmetadata")
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SongMetadataModel {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
   @Column(name = "song_year")
   private String year;
   private String name;
   private String artist;
   private String album;
   private String length;
   @Column(unique = true)
   private Long resourceId;

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;

      if (!(o instanceof SongMetadataModel))
         return false;

      SongMetadataModel that = (SongMetadataModel) o;

      return new EqualsBuilder().append(year, that.year).append(name, that.name).append(artist, that.artist).append(album, that.album)
            .append(length, that.length).append(resourceId, that.resourceId).isEquals();
   }

   @Override public int hashCode() {
      return new HashCodeBuilder(17, 37).append(year).append(name).append(artist).append(album).append(length).append(resourceId).toHashCode();
   }

}
