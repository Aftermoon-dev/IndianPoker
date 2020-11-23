package dev.aftermoon.indianpoker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import dev.aftermoon.indianpoker.databinding.ActivityGameBinding;

public class GameActivity extends AppCompatActivity {
    Handler handler;
    private ActivityGameBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Activity 들어올 땐 System UI 숨기기
        Util.hideSystemUI(getWindow());

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                // 플레이어의 입력을 대기중인 상황
                if(msg.what == 1) {
                    // 모든 버튼 비활성화 해제
                }
                return true;
            }
        });

        // 게임 쓰레드 시작
        GameThread gameThread = new GameThread();
        gameThread.start();
    }


    @Override
    protected void onDestroy() {
        // Activity 나갈땐 System UI 다시 나타내기
        Util.showSystemUI(getWindow());
        super.onDestroy();
    }

    class GameThread extends Thread {
        // 모든 유저의 현재 소지 금액을 저장할 변수
        private int[] userCoin;

        // 모든 유저의 현재 카드를 저장할 변수
        // 현재 카드가 -1일 경우, 게임을 진행중인 플레이어가 아님
        private int[] userCard;

        // 컴퓨터가 확률을 계산할 때 사용할 가능 한 카드 갯수 저장 배열
        private int[][] useCard;

        // 현재 남은 각 카드 수
        private int[] currentCard;

        // 전체 카드 중 남은 수
        private int currentCardCnt;

        // 플레이어 베팅 방법 (-1 : 베팅 선택 안됨)
        private int playerBetMethod = -1;

        /*
         * 플레이어에게 Random한 카드를 지급한다
         * @author Minjae Seon
         * @return 가지고 있을 카드 넘버 (-1 = 카드가 없음)
         */
        private int giveRandomCard() {
            // 현재 남은 카드가 1장 이상일 경우
            if (currentCardCnt > 0) {
                // 랜덤으로 1~10중 하나 뽑기
                Random random = new Random();
                int cardNum = random.nextInt(10) + 1;

                // 해당 카드의 잔여 카드 수가 1장 이상인경우
                if (currentCard[cardNum - 1] > 0) {
                    // 해당 카드의 잔여 수 1장 제거
                    currentCard[cardNum - 1]--;
                    currentCardCnt--;

                    // 해당 카드 숫자 리턴
                    return cardNum;
                }
                // 해당 카드의 잔여 수가 없는 경우
                else {
                    // 재귀 호출로 다른 카드를 정하도록 함
                    return giveRandomCard();
                }
            }
            return -1;
        }

        /*
         * 모든 변수 초기화
         * @param playerCnt 플레이중인 인원 수
         * @author Minjae Seon
         */
        private void resetVariable(int playerCnt) {
            // userCoin과 userCard에서는 무조건 0번째가 실제 Player의 값

            // 아직 배팅 안했으므로 -1
            playerBetMethod = -1;

            // user들이 소지하고 있는 코인
            userCoin = new int[4];
            Arrays.fill(userCoin, 100);

            // user들이 현재 소지하고 있는 카드
            // userCard[0]이 게임이 끝나기 전까지는 UI에 노출되서는 안됨 (플레이어의 카드)
            userCard = new int[4];
            Arrays.fill(userCoin, -1);

            // useCard는 현재 나올 수 있는 카드를 담고 있는 배열
            useCard = new int[4][10];
            for (int i = 0; i < 10; i++) {
                useCard[0][i] = 2;
            }

            // 현재 남은 카드 수 배열
            currentCard = new int[10];
            // 모두 2장씩 있으므로 2로 초기화
            Arrays.fill(currentCard, 2);

            // 현재 남은 카드 수 배열
            currentCardCnt = 20;
        }

        /*
         * Computer의 베팅 방법 결정 계산 Method
         * @param playerNumber 계산을 할 기준 플레이어 번호
         * @author Minjae Seon, Hyeonho Shin
         */
        private int getSingleBetMethod(int playerNumber) {
            int betMethod = -1;
            int winN = 0;
            int loseN = 0;

            // 플레이어의 현재 카드를 경계로 나누어서 내가 이길 카드를 가지고 있는지 혹은 지는 카드를 가지고 있는지 확률 계산
            for (int i = 0; i < 10; i++) {
                if (i < currentCard[0] - 1) {
                    loseN = loseN + useCard[playerNumber][i];
                } else if (i > currentCard[0]) {
                    winN = winN + useCard[playerNumber][i];
                }
            }

            int winPercent = (int) ((float) winN / (winN + loseN) * 100);
            int losePercent = (int) ((float) loseN / (winN + loseN) * 100);

            return betMethod;
        }

        private void finishGame(int winnerID) {
            // Winner 결정 - Argument 1에 승자 아이디를 담음
            Message winnerMessage = Message.obtain();
            winnerMessage.what = 2;
            winnerMessage.arg1 = winnerID;
            handler.sendMessage(winnerMessage);
        }

        public void startGame(int playerCnt) {
            // 모든 변수 초기화
            resetVariable(playerCnt);

            // 총 배팅 금액
            int totalBet = 0;

            // 각 플레이어 배팅 금액
            int[] playerBet = new int[4];
            Arrays.fill(playerBet, 0);

            // 누군가가 코인이 없거나 사용자가 포기 이전까지 게임 지속을 위해 무한 Loop
            while (true) {

                // 누군가의 코인이 0 이하라면
                if (userCoin[0] <= 0 || userCoin[1] <= 0) {
                    // 게임 끝내기
                    if (userCoin[0] <= 0) finishGame(1);
                    else finishGame(0);

                    // 루프 탈출
                    break;
                }

                // 유저들에게 카드 지급
                userCard[0] = giveRandomCard();
                userCard[1] = giveRandomCard();

                // 한 명이라도 카드가 없어서 못 받았을 경우 (예외처리)
                if (userCard[0] == -1 || userCard[1] == -1) {
                    // 돈 더 많은 사람이 이기는 걸로 처리하고 게임 끝내기
                    if (userCoin[0] < userCoin[1]) finishGame(1);
                    else finishGame(0);

                    // 루프 탈출
                    break;
                }

                // 참가비 배팅
                userCoin[0]--;
                userCoin[1]--;

                // 배팅 금액은 현재 1씩
                playerBet[0] = 1;
                playerBet[1] = 1;

                // 총 배팅금액은 2
                totalBet = 2;

                // 컴퓨터가 확률 계산에 사용할 배열 값 처리
                useCard[1][userCard[0]]--;

                // PC의 배팅 방법 결정
                int PCBetMethod = getSingleBetMethod(1);

                // 플레이어의 Bet Method가 -1이라면 대기해야 함
                while (playerBetMethod == -1) {
                    try {
                        handler.sendEmptyMessage(1);
                        wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void run() {
            startGame(2);
        }
    }
}
