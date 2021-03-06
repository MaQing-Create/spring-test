package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.util.Strings;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
    @Id
    @GeneratedValue
    private int id;
    private String eventName;
    private String keyword;
    @Builder.Default
    private int voteNum = 0;
    @ManyToOne
    private UserDto user;
    @Builder.Default
    private int rank = 0;
    @Builder.Default
    private int price = 0;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEvent")
    private List<TradeDto> trade;


    public String toString(){
        return Integer.toString(id);
    }
}
