package test;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RabbitMQConnectionTest extends BastTest{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private Listener listener;

    @Test
    public void testRabbitMQConnection() throws InterruptedException {
        String message = "Test message";
        rabbitTemplate.convertAndSend("testQueue", message);
        Thread.sleep(1000); // 等待消息被处理

        // 检查监听器是否收到消息
        verify(listener, times(1)).receiveMessage(message);
    }

    // 定义一个简单的 RabbitMQ 监听器，用于接收消息
    public static class Listener {
        @RabbitListener(queues = "testQueue")
        public void receiveMessage(String message) {
            System.out.println("Received message: " + message);
        }
    }
}
