package dev.aftermoon.indianpoker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import dev.aftermoon.indianpoker.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;

    // 플레이 인원
    private final int PLAYER_NUM = 2;

    // 실제 사람은 0번째 플레이어
    private final int PLAYER = 0;

    // 컴퓨터는 1번째 플레이어
    private final int COMPUTER = 1;

    // 게임 전체에서 각 카드의 남은 수량을 담는 배열
    private final int[] cardList = new int[10];

    // 플레이어들이 가지고 있는 카드를 담는 배열
    private final int[] playerCard = new int[2];

    // 플레이어들의 소지 코인을 담는 배열
    private final int[] playerCoin = new int[2];

    // 플레이어들이 베팅한 방법을 담는 배열
    private final int[] playerBetMethod = new int[2];

    // 플레이어들의 현재 베팅 코인
    private final int[] playerBetCoin = new int[2];

    // 컴퓨터가 확률 계산을 위해 남은 카드의 수량을 담는 배열
    private final int[] computerCardList = new int[10];

    // 현재 게임 내에서의 총 베팅 코인
    private int currentAllBetCoin;

    // 현재 게임 내에서의 남은 카드 수의 갯수
    private int currentRemainCard;

    private String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Bundle bundle = getIntent().getExtras();
        userName = bundle != null ? bundle.getString("name") : "플레이어";
        if(userName == null || userName.isEmpty()) userName = "플레이어";

        binding.tvPlayerName.setText(userName);

        // Activity 들어올 땐 System UI 숨기기
        Util.hideSystemUI(getWindow());

        // 버튼 이벤트 설정
        setButtonEvent();

        // 변수 초기화
        resetVariable();

        // 게임 시작
        gameStart(true);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        // Activity 나갈땐 System UI 다시 나타내기
        Util.showSystemUI(getWindow());
        super.onDestroy();
    }

    private void setButtonEvent() {
        binding.btnDie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBetMethod[PLAYER] = 0;
                setPlayerBetMethodText();
            }
        });

        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBetMethod[PLAYER] = 1;
                setPlayerBetMethodText();
            }
        });

        binding.btnQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBetMethod[PLAYER] = 2;
                setPlayerBetMethodText();
            }
        });

        binding.btnHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBetMethod[PLAYER] = 3;
                setPlayerBetMethodText();
            }
        });

        binding.btnAllin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBetMethod[PLAYER] = 4;
                setPlayerBetMethodText();
            }
        });
    }

    private void resetVariable() {
        Arrays.fill(cardList, 2);
        Arrays.fill(playerCard, 0);
        Arrays.fill(playerCoin, 100);
        Arrays.fill(playerBetCoin, 0);
        Arrays.fill(playerBetMethod, 0);
        Arrays.fill(computerCardList, 2);
        currentAllBetCoin = 0;
        currentRemainCard = 20;

        binding.tvUserCoin.setText(getString(R.string.current_coin, playerCoin[PLAYER]));
        binding.tvComputerCoin.setText(getString(R.string.current_coin, playerCoin[COMPUTER]));
        binding.tvCurrentPlayerbet.setText(getString(R.string.current_betmethod, getBetMethodText(PLAYER), calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER)));
        binding.ivCompterCard.setImageResource(R.drawable.card_blind);
        binding.ivPlayerCard.setImageResource(R.drawable.card_blind);
    }

    private int getRandomCard() {
        // 현재 남은 카드가 2장 이상이여야 게임을 진행할 수 있음
        if(currentRemainCard >= 2) {
            Random random = new Random();

            // 랜덤하게 1~10에서 하나 선택
            int card = random.nextInt(10) + 1;

            // 만약 해당 Card가 0개 초과라면
            if (cardList[card - 1] > 0) {
                // 전체 카드 중 남은 카드 수 1 제거
                currentRemainCard--;

                // 해당 카드의 남은 수 1 제거
                cardList[card - 1]--;

                // 해당 카드를 리턴
                return card;
            }
            // 카드가 없다면 Recursive하게 불러와서 겹치지 않는 카드를 가지도록 함
            else {
                return getRandomCard();
            }
        }
        // 남은 카드가 2장 미만이면 -1을 리턴
        else {
            Log.d("card", "NO CARD!");
            return -1;
        }

    }

    // 일반 게임 시작
    private void gameStart(boolean isResetBet) {
        if(playerCoin[PLAYER] < 1 && playerCoin[COMPUTER] < 1) {
            Toast.makeText(this, "모든 플레이어가 1원 이상의 돈이 있어야 시작할 수 있습니다! 게임을 종료합니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 카드 다시 가리기
        binding.ivPlayerCard.setImageResource(R.drawable.card_blind);
        binding.ivCompterCard.setImageResource(R.drawable.card_blind);

        // 코인 새로고침
        binding.tvUserCoin.setText(getString(R.string.current_coin, playerCoin[PLAYER]));
        binding.tvComputerCoin.setText(getString(R.string.current_coin, playerCoin[COMPUTER]));

        // 플레이어의 이전 베팅 방법 리셋
        for(int i = 0; i < PLAYER_NUM; i++) {
            playerBetMethod[i] = 0;
        }

        // 베팅 리셋을 하는 경우
        if(isResetBet) {
            // 전체 베팅 코인 리셋
            currentAllBetCoin = 0;
            binding.tvCurrentAllbet.setText(getString(R.string.current_betcoin, currentAllBetCoin));

            // 플레이어의 이전 베팅 코인 리셋
            for(int i = 0; i < PLAYER_NUM; i++) {
                playerBetCoin[i] = 0;
            }
        }

        for(int i = 0; i < PLAYER_NUM; i++) {
            // 각 플레이어에게 랜덤 카드 지급
            playerCard[i] = getRandomCard();

            // i가 컴퓨터일경우
            if(i == COMPUTER) {
                Log.d("start", "Computer Card Number : " + playerCard[i]);
                // 카드 이미지 변경
                int id = getResources().getIdentifier("card_" + (playerCard[i]), "drawable", getPackageName());
                binding.ivCompterCard.setImageDrawable(ResourcesCompat.getDrawable(getResources(), id, getTheme()));
            }

            // 참가비 1코인씩 베팅
            playerBet(i, 1);
        }

        // 플레이어 턴 시작
        playerTurn();
    }

    private void playerTurn() {
        // 플레이어 버튼 활성화
        enablePlayerButton();

        // 플레이어에게 메시지 출력
        Toast.makeText(this, "당신의 턴입니다! 15초 동안 배팅을 해주세요.", Toast.LENGTH_SHORT).show();

        // 현재 턴 플레이어 이름 변경
        binding.tvCurrentTurn.setText(getString(R.string.current_turn, userName));

        // 30초 타이머 작동
        CountDownTimer countDownTimer = new CountDownTimer((15*1000), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 초당 변경
                binding.tvRemainingTime.setText(getString(R.string.remain_time, (int) (millisUntilFinished / 1000) % 60));
            }

            @Override
            public void onFinish() {
                // 플레이어 버튼 비활성화
                disablePlayerButton();

                // 베팅
                playerBet(PLAYER, calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER));

                // 컴퓨터 턴으로 넘어감
                computerTurn();
            }
        };
        countDownTimer.start();
    }

    private void computerTurn() {
        // 플레이어에게 메시지 출력
        Toast.makeText(this, "안드로이드의 턴입니다!", Toast.LENGTH_SHORT).show();

        // 현재 턴 플레이어 이름 변경
        binding.tvCurrentTurn.setText(getString(R.string.current_turn, "안드로이드"));

        // 5초 타이머 작동
        CountDownTimer countDownTimer = new CountDownTimer((5*1000), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvRemainingTime.setText(getString(R.string.remain_time, (int) (millisUntilFinished / 1000) % 60));
            }

            @Override
            public void onFinish() {
                // 컴퓨터 베팅 방법 설정
                playerBetMethod[COMPUTER] = calculateComputer();
                Toast.makeText(GameActivity.this, "안드로이드는 " + getBetMethodText(COMPUTER) + "(을)를 선택했습니다!", Toast.LENGTH_SHORT).show();

                // 베팅
                playerBet(COMPUTER, calculateBetPrice(playerBetMethod[COMPUTER], COMPUTER, PLAYER));

                // 게임 결과 로드
                gameResult();
            }
        };
        countDownTimer.start();
    }

    private void gameResult() {
        // 둘 중에 한 명이라도 콜이나 다이를 한 경우
        if((playerBetMethod[PLAYER] == 0 || playerBetMethod[PLAYER] == 1) || (playerBetMethod[COMPUTER] == 0 || playerBetMethod[COMPUTER] == 1)) {
            // 카드 공개
            cardOpen();

            // 승자 확인
            final int winner = getWinner();

            if(winner == PLAYER) {
                Toast.makeText(this, "당신의 승리입니다! 5초 후 게임이 계속됩니다.", Toast.LENGTH_SHORT).show();
            }
            else if(winner == COMPUTER) {
                Toast.makeText(this, "당신의 패배입니다. 5초 후 게임이 계속됩니다.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "무승부입니다! 현재 베팅 코인이 유지됩니다! 5초 후 게임이 계속됩니다.", Toast.LENGTH_SHORT).show();
            }

            // 카드 10인데 다이하면 10원씩 깎임
            if(playerBetMethod[COMPUTER] == 0 && playerCard[COMPUTER] == 10) {
                playerCoin[COMPUTER] -= 10;
            }

            if(playerBetMethod[PLAYER] == 0 && playerCard[PLAYER] == 10) {
                playerCoin[PLAYER] -= 10;
            }

            if(winner != -1) playerCoin[winner] += currentAllBetCoin;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    gameStart(winner != -1);
                }
            }, 5000);

        }
        else {
            Toast.makeText(this, "만드는중", Toast.LENGTH_LONG).show();
        }
    }

    private int getWinner() {
        // 둘 다 다이했으면 무승부 (-1)
        if(playerBetMethod[PLAYER] == 0 && playerBetMethod[COMPUTER] == 0) {
            return -1;
        }
        // 둘 중에 한 명이라도 다이를 했으면 우승자는 자동으로 다이 안한 반대쪽 사람
        else if(playerBetMethod[PLAYER] == 0) {
            return COMPUTER;
        }
        else if(playerBetMethod[COMPUTER] == 0) {
            return PLAYER;
        }
        // 다이가 아닌 경우
        else {
            // 플레이어의 카드가 더 크면 플레이어 승리
            if(playerCard[PLAYER] > playerCard[COMPUTER]) {
                return PLAYER;
            }
            // 플레이어의 카드가 더 작으면 컴퓨터 승리
            else if(playerCard[PLAYER] < playerCard[COMPUTER]) {
                return COMPUTER;
            }
            // 둘이 같으면 무승부
            else {
                return -1;
            }
        }
    }

    private void cardOpen() {
        // 플레이어 카드 오픈
        Log.d("cardopen", "player card is " + playerCard[PLAYER]);
        int playerCardID = getResources().getIdentifier("card_" + (playerCard[PLAYER]), "drawable", getPackageName());
        binding.ivPlayerCard.setImageDrawable(ResourcesCompat.getDrawable(getResources(), playerCardID, getTheme()));

        // 컴퓨터의 계산용 카드 목록에 본인 카드 추가
        computerCardList[playerCard[COMPUTER]-1]++;
    }

    private void enablePlayerButton() {
        // 플레이어의 버튼 활성화 (베팅 가능한 금액인경우)
        binding.btnDie.setEnabled(calculateBetPrice(0, PLAYER, COMPUTER) >= 0);
        binding.btnCall.setEnabled(calculateBetPrice(1, PLAYER, COMPUTER) >= 0);
        binding.btnQuarter.setEnabled(calculateBetPrice(2, PLAYER, COMPUTER) >= 0);
        binding.btnHalf.setEnabled(calculateBetPrice(3, PLAYER, COMPUTER) >= 0);
        binding.btnAllin.setEnabled(calculateBetPrice(4, PLAYER, COMPUTER) >= 0);
    }

    private void disablePlayerButton() {
        // 플레이어의 모든 버튼 비활성화
        binding.btnDie.setEnabled(false);
        binding.btnCall.setEnabled(false);
        binding.btnQuarter.setEnabled(false);
        binding.btnHalf.setEnabled(false);
        binding.btnAllin.setEnabled(false);
    }

    private int calculateComputer() {
        int pcBetMethod = -1;
        int winN = 0;
        int loseN = 0;

        // 플레이어의 현재 카드를 경계로 나누어서 내가 이길 카드를 가지고 있는지 혹은 지는 카드를 가지고 있는지 확률 계산
        for (int i = 0; i < 10; i++) {
            if (i < playerCard[PLAYER] - 1) {
                loseN = loseN + computerCardList[i];
            } else if (i > playerCard[PLAYER] - 1) {
                winN = winN + computerCardList[i];
            }
        }

        // 상대방의 베팅에 따른 확률 조정
        double plusValue = 0.0;
        if(playerBetMethod[PLAYER] == 4) plusValue = -1.0;
        else if(playerBetMethod[PLAYER] == 3) plusValue = -0.5;
        else if(playerBetMethod[PLAYER] == 2) plusValue = -0.25;
        else if(playerBetMethod[PLAYER] == 1) plusValue = 0.25;
        else if(playerBetMethod[PLAYER] == 0) plusValue = 10.0;

        // 이길 확률 계산
        double winP = (((float) winN / (winN + loseN) * 100) + plusValue);

        // 이길 확률이 50% 이하면 다이
        if (winP < 50) pcBetMethod = 0;
        // 이길 확률이 100%이거나 PC가 가진 돈이 전체 판돈 이하일경우 올인
        else if(winP == 100 || playerCoin[COMPUTER] <= currentAllBetCoin) pcBetMethod = 4;
        // 이길 확률이 90% 이상이면서 PC가 가진 돈이 플레이어의 이전 판돈 + 현재 총 배팅금액의 절반 이상일 경우 하프
        else if(winP >= 90 && playerCoin[COMPUTER] >= playerBetCoin[PLAYER] + ((playerBetCoin[PLAYER] + currentAllBetCoin) / 2)) pcBetMethod = 3;
        // 이길 확률이 80% 이상이면서 PC가 가진 돈이 플레이어의 이전 판돈 + 현재 총 배팅금액의 4분의 1 이상일 경우 쿼터
        else if(winP >= 80 && playerCoin[COMPUTER] >= playerBetCoin[PLAYER] + ((playerBetCoin[PLAYER] + currentAllBetCoin) / 4)) pcBetMethod = 2;
        // 그 이외의 경우에는 콜
        else pcBetMethod = 1;

        return pcBetMethod;
    }

    private boolean playerBet(int playerNum, int betCoin) {
        // Player가 가진 코인 수가 베팅을 원하는 코인 수보다 같거나 많다면
        if(playerCoin[playerNum] >= betCoin) {
            // 베팅 적용
            playerCoin[playerNum] -= betCoin;
            playerBetCoin[playerNum] += betCoin;
            currentAllBetCoin += betCoin;

            // 텍스트 설정
            binding.tvUserCoin.setText(getString(R.string.current_coin, playerCoin[PLAYER]));
            binding.tvComputerCoin.setText(getString(R.string.current_coin, playerCoin[COMPUTER]));
            binding.tvCurrentAllbet.setText(getString(R.string.current_betcoin, currentAllBetCoin));

            // 베팅 성공했으므로 true 리턴
            return true;
        }
        // Player가 가진 코인 수가 베팅을 원하는 코인 수보다 적다면 베팅 실패이므로 false 리턴
        else return false;
    }

    private String getBetMethodText(int player) {
        String betString = "";
        if(playerBetMethod[player] == 0) betString = "다이";
        else if(playerBetMethod[player] == 1) betString = "콜";
        else if(playerBetMethod[player] == 2) betString = "쿼터";
        else if(playerBetMethod[player] == 3) betString = "하프";
        else if(playerBetMethod[player] == 4) betString = "올인";
        return betString;
    }

    private void setPlayerBetMethodText() {
        binding.tvCurrentPlayerbet.setText(getString(R.string.current_betmethod, getBetMethodText(PLAYER), calculateBetPrice(playerBetMethod[PLAYER], PLAYER, COMPUTER)));
    }

    private int calculateBetPrice(int method, int player, int player2) {
        // 콜
        if(method == 1) {
            if(playerBetCoin[player2] <= playerCoin[player]) return playerBetCoin[player2];
            else return -1;
        }
        // 쿼터
        else if(method == 2) {
            int qPrice = playerBetCoin[player2] + ((playerBetCoin[player2] + currentAllBetCoin) / 4);
            if(qPrice <= playerCoin[player]) return qPrice;
            else return -1;
        }
        // 하프
        else if(method == 3) {
            int hPrice = playerBetCoin[player2] + ((playerBetCoin[player2] + currentAllBetCoin) / 2);
            if(hPrice <= playerCoin[player]) return hPrice;
            else return -1;
        }
        // 올인
        else if(method == 4) {
            return playerCoin[player];
        }
        else return 0;
    }
}
