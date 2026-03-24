package back.pickd.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.services.calendar.Calendar;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleCalenderConfig {
    private static final String APPLICATION_NAME = "desktop";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIR = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_PATH = "/desktop.json";

    @Bean
    public Calendar calendarClient() throws Exception {
        // 1) HTTP transport 생성
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 2) 자격 증명 파일(desktop.json) 로드 및 null 체크
        // try-with-resources를 사용하여 사용 후 스트림을 자동으로 닫습니다.
        try (InputStream in = getClass().getResourceAsStream(CREDENTIALS_PATH)) {

            if (in == null) {
                // 파일 경로가 잘못되었거나 빌드 폴더에 파일이 없을 때 발생합니다.
                throw new FileNotFoundException("설정된 경로에서 파일을 찾을 수 없습니다: " + CREDENTIALS_PATH
                        + "\n[팁] src/main/resources 폴더에 파일이 있는지 확인하세요.");
            }

            GoogleClientSecrets clientSecrets;
            try {
                clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            } catch (Exception e) {
                // JSON 파일 내용이 구글 자격증명 형식과 맞지 않을 때 발생합니다.
                throw new RuntimeException("JSON 파싱 실패! 'desktop.json' 파일의 형식이 '데스크톱 앱'용인지 확인하세요.", e);
            }

            // 3) OAuth 흐름 구성
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIR)))
                    .setAccessType("offline")
                    .build();

            // 4) 로컬 서버로 인증 코드 수신 (데스크톱 앱 방식)
            LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(8888)
                    .build();

            // 5) 사용자 인증 및 Credential 생성
            // 인증을 성공하면 지정된 TOKENS_DIR에 토큰이 저장됩니다.
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver)
                    .authorize("user");

            // 6) Calendar 클라이언트 빌드
            return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }
}