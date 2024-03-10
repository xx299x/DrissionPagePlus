import com.ll.DrissonPage.page.ChromiumPage;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>点击
 * @original DrissionPage
 */
public class test {
    public static void main(String[] args) {
        System.out.println(Thread.activeCount());
        for (int i = 0; i < 100; i++) {
            System.out.println("运行次数为:--->" + i);
            ChromiumPage instance = null;
            try {
                instance = ChromiumPage.getInstance();
                instance.get("https://cdn.midjourney.com/4ae03c45-be8e-42fd-86aa-ebecda0babf3/0_1.png");
                Thread.sleep(10000);
                instance.set().window().size(6, null);
                instance.disconnect();
                instance.handleAlert(true);
                instance = ChromiumPage.getInstance();
//                System.out.println("运行成功数量为：" + ok++);
            } catch (Exception e) {
                e.printStackTrace();
//                System.out.println("运行失败数量为：" + error++);
            } finally {
                assert instance != null;
                try {
                    instance.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("存活线程数量为" + Thread.activeCount());
            }
        }
        System.out.println(Thread.activeCount());
        System.out.println(6);
    }
}
