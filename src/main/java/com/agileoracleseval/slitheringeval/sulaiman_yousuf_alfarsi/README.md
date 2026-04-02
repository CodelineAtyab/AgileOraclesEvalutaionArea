# CLI Snake Game (Java)

A simple command-line Snake game where the snake moves using arguments and saves its state in a file.

---

## ▶ Run

First build the project:

Then run from terminal:

reset:
java -cp target/classes com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi.MoveSnake reset

move the snake:
java -cp target/classes com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi.MoveSnake up 
java -cp target/classes com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi.MoveSnake down
java -cp target/classes com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi.MoveSnake right
java -cp target/classes com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi.MoveSnake left

## Usage

Directions:
- up
- down
- left
- right

---

## Notes

- Snake wraps around edges
- Cannot collide with itself
- State is saved in `map.txt`
- Use `reset` to restart the game