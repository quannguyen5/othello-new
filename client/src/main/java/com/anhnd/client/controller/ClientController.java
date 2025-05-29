package com.anhnd.client.controller;

import com.anhnd.client.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;


@Controller
public class ClientController {

    @Autowired
    private HttpSession session;

    @Value("${member.service.url}")
    private String memberServiceUrl;

    @Value("${bot.service.url}")
    private String botServiceUrl;

    @Value("${result.service.url}")
    private String resultServiceUrl;

    @Value("${match.service.url}")
    private String matchServiceUrl;

    @GetMapping("/login")
    public String login() {
        if (session.getAttribute("loggedInMember") != null) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/home")
    public String home(Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUserId", loggedInMember.getId());
        return "home";
    }

    @GetMapping("/play")
    public String play() {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }
        return "play";
    }

    @GetMapping("/play-with-friend")
    public String playWithFriend() {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }
        return "play-with-friend";
    }

    @GetMapping("/select-bot")
    public String selectBot() {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }
        return "select-bot-info";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        Member loginMember = new Member();
        loginMember.setUsername(username);
        loginMember.setPassword(password);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Member> httpEntityRequest = new HttpEntity<>(loginMember, headers);
        RestTemplate restTemplate = new RestTemplate();
        String externalServiceUrl = memberServiceUrl + "/login";
        ResponseEntity<Member> responseEntity = restTemplate.exchange(
                externalServiceUrl,
                HttpMethod.POST,
                httpEntityRequest,
                new ParameterizedTypeReference<Member>() {}
        );
        loginMember = responseEntity.getBody();

        if (loginMember != null) {
            session.setAttribute("loggedInMember", loginMember);
            redirectAttributes.addFlashAttribute("currentUserId", loginMember.getId());
            return "redirect:/home";
        } else {
            redirectAttributes.addFlashAttribute("loginMessage", "Tên đăng nhập hoặc mật khẩu không đúng");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        String externalServiceUrl = memberServiceUrl + "/logout";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Member> request = new HttpEntity<>(loggedInMember, headers);
        try {
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(
                    externalServiceUrl,
                    HttpMethod.POST,
                    request,
                    Boolean.class
            );
            boolean logout = Boolean.TRUE.equals(responseEntity.getBody());
            if(logout) {
                session.invalidate();
                return "redirect:/login";
            }
        } catch (Exception e) {
            return "redirect:/home";
        }
        return "redirect:/home";
    }

    @GetMapping("/statistic")
    public String statistic(Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        String externalServiceUrl = memberServiceUrl + "list-friend?memberId=" + loggedInMember.getId();
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Member>> friendsResponse = restTemplate.exchange(
                    externalServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Member>>() {}
            );

            List<Member> friends = friendsResponse.getBody();

            int currentUserRank = 0;
            for (int i = 0; i < friends.size(); i++) {
                if (friends.get(i).getId() == loggedInMember.getId()) {
                    currentUserRank = i + 1;
                    break;
                }
            }

            model.addAttribute("allPlayers", friends);
            model.addAttribute("currentUser", loggedInMember);
            model.addAttribute("currentUserRank", currentUserRank);

            model.addAttribute("friends", friends);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể lấy dữ liệu từ server");
        }

        return "statistic";
    }

    @GetMapping("/statistic-detail/{memberId}")
    public String statisticDetail(@PathVariable int memberId, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        RestTemplate restTemplate = new RestTemplate();

        try {
            String memberUrl = memberServiceUrl + "get-member?memberId=" + memberId;
            ResponseEntity<Member> memberResponse = restTemplate.exchange(
                    memberUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Member>() {}
            );
            Member selectedMember = memberResponse.getBody();

            String listResultUrl = resultServiceUrl + "get-list-result?memberId=" + memberId;
            ResponseEntity<List<Result>> resultResponse = restTemplate.exchange(
                    listResultUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Result>>() {}
            );
            List<Result> results = resultResponse.getBody();

            int totalMatches = results.size();
            int wins = 0;
            int draws = 0;
            int losses = 0;

            int vsCurrentUserWins = 0;
            int vsCurrentUserDraws = 0;
            int vsCurrentUserLosses = 0;

            for (Result result : results) {
                if (result.getMemberA().getId() == memberId) {
                    if (result.getMemberB().getId() > 0) {
                        if (result.getMemberB().getId() == loggedInMember.getId()) {
                            if (result.getResAtoB() == 1) {
                                wins++;
                                vsCurrentUserWins++;
                            } else {
                                losses++;
                                vsCurrentUserLosses++;
                            }
                        } else {
                            if (result.getResAtoB() == 1) {
                                wins++;
                            } else {
                                losses++;
                            }
                        }
                    } else if(result.getBot().getId() > 0) {
                        if(result.getResAToBot() == 1) {
                            wins++;
                        } else {
                            losses++;
                        }
                    }
                }
                else if (result.getMemberB().getId() == memberId) {
                    if (result.getMemberA().getId() == loggedInMember.getId()) {
                        if (result.getResAtoB() == 0) {
                            wins++;
                            vsCurrentUserWins++;
                        } else {
                            losses++;
                            vsCurrentUserLosses++;
                        }
                    } else {
                        if (result.getResAtoB() == 0) {
                            wins++;
                        } else {
                            losses++;
                        }
                    }
                }
            }

            double winRate = totalMatches > 0 ? (double) wins / totalMatches * 100 : 0;
            String formattedWinRate = String.format("%.1f%%", winRate);

            model.addAttribute("member", selectedMember);
            model.addAttribute("totalMatches", totalMatches);
            model.addAttribute("wins", wins);
            model.addAttribute("draws", draws);
            model.addAttribute("losses", losses);
            model.addAttribute("winRate", formattedWinRate);

            model.addAttribute("vsCurrentUserWins", vsCurrentUserWins);
            model.addAttribute("vsCurrentUserDraws", vsCurrentUserDraws);
            model.addAttribute("vsCurrentUserLosses", vsCurrentUserLosses);

            model.addAttribute("currentUser", loggedInMember);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "statistic-detail";
    }

    @GetMapping("/type-result-detail")
    public String typeResultDetail(@RequestParam("currentUserId") int currentUserId,
                                   @RequestParam("memberId") int memberId,
                                   @RequestParam("type") int type,
                                   Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }
        String externalServiceUrl = resultServiceUrl + "get-list-result-to-opponent?" +
                "memberId=" + memberId + "&opponentId=" + currentUserId + "&type=" + type;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Result>> listResultResponse = restTemplate.exchange(
                    externalServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Result>>(){}
            );
            List<Result> listResults = listResultResponse.getBody();
            model.addAttribute("resultType", type);
            model.addAttribute("listResults", listResults);

            return "type-result-detail";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create-bot-match")
    @ResponseBody
    public Map<String, Object> createBotMatch(@RequestBody Map<String, String> request) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        Map<String, Object> response = new HashMap<>();

        if (loggedInMember == null) {
            response.put("success", false);
            response.put("error", "Not logged in");
            return response;
        }

        String difficulty = request.get("difficulty");
        String color = request.get("color");

        // Get bot based on difficulty
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String algorithm = difficulty.equals("easy") ? "random" : "minimax";
        String externalBotServiceUrl = botServiceUrl + "get-bot?algorithm=" + algorithm;

        Bot bot = null;
        try {
            ResponseEntity<Bot> responseEntity = restTemplate.exchange(
                    externalBotServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Bot>() {}
            );
            bot = responseEntity.getBody();
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get bot information");
            return response;
        }

        // Create challenge
        Challenge challenge = new Challenge();
        challenge.setChallenger(loggedInMember);
        challenge.setBot(bot);
        challenge.setStatus("ACCEPTED");
        challenge.setWithBot(1);
        // Set white/black based on color preference
        if (color.equals("white")) {
            challenge.setIsWhiteRequester(1);
        } else if (color.equals("black")) {
            challenge.setIsWhiteRequester(0);
        } else {
            challenge.setIsWhiteRequester(Math.random() < 0.5 ? 1 : 0);
        }

        String externalMemberServiceUrl = memberServiceUrl + "create-challenge";
        try {
            HttpEntity<Challenge> httpEntityRequest = new HttpEntity<>(challenge, headers);
            ResponseEntity<Challenge> responseEntity = restTemplate.exchange(
                    externalMemberServiceUrl,
                    HttpMethod.POST,
                    httpEntityRequest,
                    new ParameterizedTypeReference<Challenge>() {}
            );
            challenge = responseEntity.getBody();
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to create challenge");
            return response;
        }

        // Create match
        if(challenge != null) {
            Match match = new Match();
            match.setChallenge(challenge);

            match.setWhiteToBlack(-1);

            String externalMatchServiceUrl = matchServiceUrl + "create-match";
            try {
                System.out.println(1);
                HttpEntity<Match> httpEntityRequest = new HttpEntity<>(match, headers);
                ResponseEntity<Match> responseEntity = restTemplate.exchange(
                        externalMatchServiceUrl,
                        HttpMethod.POST,
                        httpEntityRequest,
                        new ParameterizedTypeReference<Match>() {}
                );
                match = responseEntity.getBody();
                System.out.println(0);

                if (match != null) {
                    // Store game preferences in session
                    session.setAttribute("gameDifficulty", difficulty);
                    session.setAttribute("gameColor", color);

                    response.put("success", true);
                    response.put("matchId", match.getId());
                    return response;
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", "Failed to create match");
                return response;
            }
        }

        response.put("success", false);
        response.put("error", "Failed to create game");
        return response;
    }

    @GetMapping("/play-with-bot/{matchId}")
    public String playWithBotById(@PathVariable int matchId, Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        // Pass match data to the view
        model.addAttribute("matchId", matchId);
        model.addAttribute("difficulty", session.getAttribute("gameDifficulty"));
        model.addAttribute("color", session.getAttribute("gameColor"));

        return "play-with-bot";
    }

    @PostMapping("/make-move-api")
    @ResponseBody
    public Board makeMove(@RequestBody Map<String, Object> request) {
        try {
            String algorithm = (String) request.get("algorithm");
            int color = (Integer) request.get("color");
            List<List<Integer>> boardList = (List<List<Integer>>) request.get("board");

            int[][] board = new int[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    board[i][j] = boardList.get(i).get(j);
                }
            }

            Board requestBoard = new Board();
            requestBoard.setBoard(board);
            requestBoard.setAlgorithm(algorithm);
            requestBoard.setColor(color);

            RestTemplate restTemplate = new RestTemplate();
            String url = botServiceUrl + "make-move";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            HttpEntity<Board> entity = new HttpEntity<>(requestBoard, headers);

            ResponseEntity<Board> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Board.class
            );
            return response.getBody();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/save-match-result")
    @ResponseBody
    public boolean saveMatchResult(@RequestBody Match match) {
        try {
            Member loggedInMember = (Member) session.getAttribute("loggedInMember");
            if (loggedInMember == null) {
                return false;
            }

            // Kiểm tra match ID
            if (match == null || match.getId() <= 0) {
                System.err.println("Invalid match ID: " + match.getId());
                return false;
            }

            // Không cần thiết lập thêm thông tin, chỉ cần ID và whiteToBlack
            System.out.println("Saving match result: ID=" + match.getId() + ", whiteToBlack=" + match.getWhiteToBlack());

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Debug request trước khi gửi
            String requestUrl = matchServiceUrl + "save-match-result";
            System.out.println("Sending request to: " + requestUrl);
            System.out.println("Request payload: " + match);

            HttpEntity<Match> requestEntity = new HttpEntity<>(match, headers);

            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(
                        requestUrl,
                        HttpMethod.POST,
                        requestEntity,
                        Boolean.class
                );
                System.out.println("Response received: " + response.getBody());
                return response.getBody();
            } catch (Exception e) {
                System.err.println("Error in REST call: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @GetMapping("/challenge")
    public String challenge(Model model) {
        Member loggedInMember = (Member) session.getAttribute("loggedInMember");
        if (loggedInMember == null) {
            return "redirect:/login";
        }

        String externalServiceUrl = memberServiceUrl + "list-friend?memberId=" + loggedInMember.getId();
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<List<Member>> listResponseEntity = restTemplate.exchange(
                    externalServiceUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Member>>(){}
            );
            List<Member> listMembers = listResponseEntity.getBody();
            for (Member member : listMembers) {
                if(member.getId() == loggedInMember.getId()) {
                    listMembers.remove(member);
                    break;
                }
            }
            model.addAttribute("listMembers", listMembers);
            return "challenge";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
