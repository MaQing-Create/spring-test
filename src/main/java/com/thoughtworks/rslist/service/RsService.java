package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.api.RsController;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.tools.CommonMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RsService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;
    final CommonMethod commonMethod;
    final TradeRepository tradeRepository;

    public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, CommonMethod commonMethod, TradeRepository tradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.commonMethod = commonMethod;
        this.tradeRepository = tradeRepository;
    }

    public void vote(Vote vote, int rsEventId) {
        Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
        Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
        if (!rsEventDto.isPresent()
                || !userDto.isPresent()
                || vote.getVoteNum() > userDto.get().getVoteNum()) {
            throw new RuntimeException();
        }
        VoteDto voteDto =
                VoteDto.builder()
                        .localDateTime(vote.getTime())
                        .num(vote.getVoteNum())
                        .rsEvent(rsEventDto.get())
                        .user(userDto.get())
                        .build();
        voteRepository.save(voteDto);
        UserDto user = userDto.get();
        user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
        userRepository.save(user);
        RsEventDto rsEvent = rsEventDto.get();
        rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
        rsEventRepository.save(rsEvent);
    }

    public void buy(Trade trade, int id) {
        if (trade.getRank() < 1) throw new RequestNotValidException("Sorry! Rank should larger than zero!");
        List<RsEventDto> rsEventDtos = sortRsEventList(rsEventRepository.findAll());
        RsEventDto rsEventDto = commonMethod.getElementFromList(id, rsEventDtos);
        Optional<RsEventDto> rsEventDtoAtRank = rsEventRepository.findByRank(trade.getRank());
        if (rsEventDtoAtRank.isPresent() && rsEventDtoAtRank.get().getPrice() >= trade.getAmount()) {
            throw new RequestNotValidException("Sorry! Your offer is too low!");
        }
        rsEventDto.setRank(trade.getRank());
        rsEventDto.setPrice(trade.getAmount());
        rsEventRepository.save(rsEventDto);
        if (rsEventDtoAtRank.isPresent()) {
            int idee = rsEventDtoAtRank.get().getId();
            rsEventRepository.deleteById(idee);
        }
        TradeDto tradeDto =
                TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build();
//        TradeDto tradeDto = TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).build();
        tradeRepository.save(tradeDto);
    }

    public List<RsEventDto> sortRsEventList(List<RsEventDto> list) {
        if (list.size() < 2)
            return list;
        List<RsEventDto> pricedRsEventDtos = new ArrayList<RsEventDto>();
        for (int i = list.size() - 1; i > -1; i--) {
            if (list.get(i).getRank() > 0) {
                pricedRsEventDtos.add(list.get(i));
                list.remove(i);
            }
        }
        Collections.sort(pricedRsEventDtos, new Comparator<RsEventDto>() {
            @Override
            public int compare(RsEventDto o1, RsEventDto o2) {
                return o1.getRank() - o2.getRank();
            }
        });
        Collections.sort(list, new Comparator<RsEventDto>() {
            @Override
            public int compare(RsEventDto o1, RsEventDto o2) {
                return o2.getVoteNum() - o1.getVoteNum();
            }
        });
        pricedRsEventDtos.stream().forEach(rsEventDto -> {
            list.add(rsEventDto.getRank() - 1, rsEventDto);
        });
        return list;
    }

}
