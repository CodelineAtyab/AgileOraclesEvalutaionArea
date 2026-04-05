# CLI Snake Game (Java)

A simple command-line Snake game where the snake moves using arguments and saves its state in a file.

---

## ▶ Run

Navigate to the source folder first:
```bash
cd src\main\java\com\agileoracleseval\slitheringeval\sulaiman_yousuf_alfarsi
```

**Reset the map:**
```bash
java MoveSnake.java reset
```

**Move the snake:**
```bash
java MoveSnake.java up
java MoveSnake.java down
java MoveSnake.java left
java MoveSnake.java right
```

**Move multiple steps:**
```bash
java MoveSnake.java right 3
java MoveSnake.java up 2
```
---

## Usage

| Argument | Description |
|----------|-------------|
| `up` | Move snake up |
| `down` | Move snake down |
| `left` | Move snake left |
| `right` | Move snake right |
| `reset` | Reset map to original state |

An optional second argument sets the number of steps (default is 1).

---



## Notes

- Snake wraps around edges
- Cannot collide with itself
- State is saved automatically in `map.txt` after every move
- Use `reset` to restart the game