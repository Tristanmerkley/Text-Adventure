package zork;

public class welcome {
        public static final String TEXT_RED = "\u001B[31m";
        public static final String TEXT_RESET = "\u001B[0m";

        public static void main(String[] args) {
                title();
        }

        public static void title() {
                slowtext(TEXT_RED
                                + "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------",
                                0);
                slowtext(
                                "                                                                                                                                                                                                                               ",
                                1);
                slowtext(
                                "███▄▄▄▄    ▄██████▄        ▄██████▄  ███▄▄▄▄      ▄████████         ▄████████    ▄████████  ▄████████    ▄████████    ▄███████▄    ▄████████    ▄████████      ████████▄     ▄████████    ▄████████     ███        ▄█    █▄    ",
                                1);
                slowtext(
                                "███▀▀▀██▄ ███    ███      ███    ███ ███▀▀▀██▄   ███    ███        ███    ███   ███    ███ ███    ███   ███    ███   ███    ███   ███    ███   ███    ███      ███   ▀███   ███    ███   ███    ███ ▀█████████▄   ███    ███   ",
                                1);
                slowtext(
                                "███   ███ ███    ███      ███    ███ ███   ███   ███    █▀         ███    █▀    ███    █▀  ███    █▀    ███    ███   ███    ███   ███    █▀    ███    █▀       ███    ███   ███    █▀    ███    ███    ▀███▀▀██   ███    ███   ",
                                1);
                slowtext(
                                "███   ███ ███    ███      ███    ███ ███   ███  ▄███▄▄▄           ▄███▄▄▄       ███        ███          ███    ███   ███    ███  ▄███▄▄▄       ███             ███    ███  ▄███▄▄▄       ███    ███     ███   ▀  ▄███▄▄▄▄███▄▄ ",
                                1);
                slowtext(
                                "███   ███ ███    ███      ███    ███ ███   ███ ▀▀███▀▀▀          ▀▀███▀▀▀     ▀███████████ ███        ▀███████████ ▀█████████▀  ▀▀███▀▀▀     ▀███████████      ███    ███ ▀▀███▀▀▀     ▀███████████     ███     ▀▀███▀▀▀▀███▀  ",
                                1);
                slowtext(
                                "███   ███ ███    ███      ███    ███ ███   ███   ███    █▄         ███    █▄           ███ ███    █▄    ███    ███   ███          ███    █▄           ███      ███    ███   ███    █▄    ███    ███     ███       ███    ███   ",
                                1);
                slowtext(
                                "███   ███ ███    ███      ███    ███ ███   ███   ███    ███        ███    ███    ▄█    ███ ███    ███   ███    ███   ███          ███    ███    ▄█    ███      ███   ▄███   ███    ███   ███    ███     ███       ███    ███   ",
                                1);
                slowtext(
                                " ▀█   █▀   ▀██████▀        ▀██████▀   ▀█   █▀    ██████████        ██████████  ▄████████▀  ████████▀    ███    █▀   ▄████▀        ██████████  ▄████████▀       ████████▀    ██████████   ███    █▀     ▄████▀     ███    █▀    ",
                                1);
                slowtext(
                                "                                                                                                                                                                                                                               ",
                                1);
                slowtext(
                                "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------"
                                                + TEXT_RESET,
                                0);
        }

        public static void slowtext(String message, int textRate) {
                for (int i = 0; i < message.length(); i++) {
                        String temp = message.substring(i, i + 1);
                        System.out.print(temp);
                        try {
                                Thread.sleep(textRate);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }

                }
                System.out.println("");
        }
}
