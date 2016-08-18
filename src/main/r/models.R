# spread
d1 = read.csv("hockey_data.csv")
m1 = lm(formula = score_diff ~ home_corsi_rel + away_corsi_rel, data = d1)

# over/under
d2 = read.csv("hockey_data_ou.csv")
m2 = lm(formula = score_sum ~ home_corsi_rel + away_corsi_rel, data = d2)

# home score
d3 = read.csv("hockey_data_home.csv")
m3 = lm(formula = home_score ~ home_corsi_rel + away_corsi_rel, data = d3)

# away score
d4 = read.csv("hockey_data_away.csv")
m4 = lm(formula = away_score ~ home_corsi_rel + away_corsi_rel, data = d4)

with(d1, plot(home_corsi_rel, score_diff))
abline(m1)
with(d1, cor.test(home_corsi_rel, score_diff))
