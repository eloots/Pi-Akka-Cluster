tnr() {
   tmux new -s run
}

tr() {
   tmux attach -t run
}

tnl() {
   tmux new -s log
}

tl() {
   tmux attach -t log
}

tkillall() {
   for session in `tmux ls | sed -e 's/:.*//'`;do
     tmux kill-session -t $session
   done
}