package dev.nick.app.automachine;

interface IInputInjector {
    void home();
    void back();
    void menu();
    void tap(int x, int y);
    void swipe(int x1, int y1, int x2, int y2, long duration);
    void text(String text);
}
