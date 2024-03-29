package zork;

public class welcome {
        public static final String TEXT_RED = "\u001B[31m";
        public static final String TEXT_RESET = "\u001B[0m";

        /**
         * prints out the welcome message, using the specified colours.
         */
        public static void title() {
                slowtext(TEXT_RED + "-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------", 0);
                System.out.println("");
                slowtext("                                                                                                                                                                                                                               ", 1);
                System.out.println("");
                slowtext("███▄▄▄▄    ▄██████▄        ▄██████▄  ███▄▄▄▄      ▄████████         ▄████████    ▄████████  ▄████████    ▄████████    ▄███████▄    ▄████████    ▄████████      ████████▄     ▄████████    ▄████████     ███        ▄█    █▄    ", 1);
                System.out.println("");
                slowtext("███▀▀▀██▄ ███    ███      ███    ███ ███▀▀▀██▄   ███    ███        ███    ███   ███    ███ ███    ███   ███    ███   ███    ███   ███    ███   ███    ███      ███   ▀███   ███    ███   ███    ███ ▀█████████▄   ███    ███   ", 1);
                System.out.println("");
                slowtext("███   ███ ███    ███      ███    ███ ███   ███   ███    █▀         ███    █▀    ███    █▀  ███    █▀    ███    ███   ███    ███   ███    █▀    ███    █▀       ███    ███   ███    █▀    ███    ███    ▀███▀▀██   ███    ███   ", 1);
                System.out.println("");
                slowtext("███   ███ ███    ███      ███    ███ ███   ███  ▄███▄▄▄           ▄███▄▄▄       ███        ███          ███    ███   ███    ███  ▄███▄▄▄       ███             ███    ███  ▄███▄▄▄       ███    ███     ███   ▀  ▄███▄▄▄▄███▄▄ ", 1);
                System.out.println("");
                slowtext("███   ███ ███    ███      ███    ███ ███   ███ ▀▀███▀▀▀          ▀▀███▀▀▀     ▀███████████ ███        ▀███████████ ▀█████████▀  ▀▀███▀▀▀     ▀███████████      ███    ███ ▀▀███▀▀▀     ▀███████████     ███     ▀▀███▀▀▀▀███▀  ", 1);
                System.out.println("");
                slowtext("███   ███ ███    ███      ███    ███ ███   ███   ███    █▄         ███    █▄           ███ ███    █▄    ███    ███   ███          ███    █▄           ███      ███    ███   ███    █▄    ███    ███     ███       ███    ███   ", 1);
                System.out.println("");
                slowtext("███   ███ ███    ███      ███    ███ ███   ███   ███    ███        ███    ███    ▄█    ███ ███    ███   ███    ███   ███          ███    ███    ▄█    ███      ███   ▄███   ███    ███   ███    ███     ███       ███    ███   ", 1);
                System.out.println("");
                slowtext(" ▀█   █▀   ▀██████▀        ▀██████▀   ▀█   █▀    ██████████        ██████████  ▄████████▀  ████████▀    ███    █▀   ▄████▀        ██████████  ▄████████▀       ████████▀    ██████████   ███    █▀     ▄████▀     ███    █▀    ", 1);
                System.out.println("");
                slowtext("                                                                                                                                                                                                                               ", 1);
                System.out.println("");
                slowtext("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------" + TEXT_RESET, 0);
                System.out.println("");
        }

        /**
         * prints out text at a slower rate, set by the parameter textRate
         * @param message
         * @param textRate
         */
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
        }
}
