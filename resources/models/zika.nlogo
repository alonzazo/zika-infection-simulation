;;*************************
;; DEFINICIÓN DE VARIABLES:
;;*************************

globals ;; Para definir las variables globales.
[
  infected
  dead
  fluorescent
  condensed
  dead-and-condensed
  alive-and-condensed
  initially-infected-cells
  prueba
]

turtles-own ;; Para definir los atributos de las tortugas.
[
  state
  chromatin-condensed
  infection-degree
  fluorescence
  infected-time
]

patches-own ;; Para definir los atributos de las parcelas.
[

]

links-own ;; Para definir los atributos de los links o conexiones.
[

]

;;********************
;; variables de breeds
;;********************



;;**************************************
;; INICIALIZACIÓN DE VARIABLES GLOBALES:
;;**************************************

to init-globals ;; Para darle valor inicial a las variables globales.
  set-default-shape turtles "circle"
  set infected [0 0 0]
  set dead [0 0 0]
  set fluorescent [0 0 0]
  set condensed [0 0 0]
  set alive-and-condensed [0 0 0]
  set dead-and-condensed [0 0 0]
end

;;**********************
;; FUNCIONES PRINCIPALES
;;**********************

to setup-experiment
  setup-blank
  crt 1
  [
    init-infected-cell
    setxy 0 0
  ]
  set initially-infected-cells 2
  update-colors
end

to setup-blank
  set initial-infected-cell-percentage 0
  setup
end

to setup ;; Para inicializar la simulación.
  ca           ;; Equivale a clear-ticks + clear-turtles + clear-patches +
               ;; clear-drawing + clear-all-plots + clear-output.

  init-globals ;; Para inicializar variables globales.

  ;; Para crear tortugas e inicializar tortugas y parcelas además.
  ask patches
  [
    init-patch
  ]

  crt cell-density * pi * max-pxcor ^ 2 ;esto garantiza que la densidad de células sea la correcta
  [
    if-else random-float 100.00 < initial-infected-cell-percentage
    [
      init-infected-cell
    ]
    [
      init-healthy-cell
    ]
    t-select-position
  ]

  set initially-infected-cells count turtles with [state = "infected"]

  update-colors

  reset-ticks  ;; Para inicializar el contador de ticks.

  set-current-plot "Reachable neighbors"
  set-plot-pen-mode 1
  set-plot-x-range ([t-report-reachable-neighbors] of (min-one-of turtles [t-report-reachable-neighbors])) ([t-report-reachable-neighbors] of (max-one-of turtles [t-report-reachable-neighbors]))
  histogram [count turtles in-radius viral-reach] of turtles

  set-current-plot "Velocity"
  set-plot-background-color gray

  set-current-plot "Cells"
  set-plot-background-color gray
end

to go
  ask turtles with [state = "infected"] ;para cada célula infectada
  [
    t-condense-chromatin
    t-infect
    t-glow
    set infected-time infected-time + 1
    t-die
  ]

  actualizar-salidas
  tick

  if (behaviorspace-run-number != 0 and (ticks mod 24) = 0)
  [output-views]

  if ticks >= total-time
    [stop]
end

;;*******************************
;; Otras funciones globales:
;;*******************************
to-report alive-cells
  report count turtles with [state = "infected" and fluorescence / 120 > marker-detection-threashold and chromatin-condensed]
end

to actualizar-salidas ;; Para actualizar todas las salidas del modelo.
  update-colors
  update-velocity-graph
end

to update-velocity-graph
  set-current-plot "Velocity"

  ;set infected update-line infected "infected cells" [ -> state = "infected" or state = "dead" ]
  set dead update-line dead "dead cells" [ -> state = "dead" ]
  set fluorescent update-line fluorescent "alive (mNeptune)" [ -> state = "infected" and fluorescence / 120 > marker-detection-threashold ]
  set condensed update-line condensed "cells with condensed chromatin" [ -> chromatin-condensed ]
  set dead-and-condensed update-line dead-and-condensed "dead and condensed" [ -> state = "dead" and chromatin-condensed ]
  set alive-and-condensed update-line alive-and-condensed "alive (mNeptune) and condensed" [ -> state = "infected" and fluorescence / 120 > marker-detection-threashold and chromatin-condensed ]
end

to-report update-line [l pen-name reporter]
  set-current-plot-pen pen-name
  set l lput (count turtles with [runresult reporter]) l
  set l remove-item 0 l
  let x (item 1 l - item 0 l)
  let y (item 2 l - item 1 l)
  let d ((x + y) / 2)
  plot d
  report l
end

to update-colors
  ifelse View = "NS3"
  [
    ask turtles with [state = "infected" or state = "dead"]
    [
      set color orange
    ]
    ask turtles with [state = "healthy"]
    [
      set color black
    ]
  ]
  [ ;else
    ifelse View = "chromatin"
    [
      ask turtles with [chromatin-condensed]
      [
        set color sky
      ]
      ask turtles with [not chromatin-condensed]
      [
        set color black
      ]
    ]
    [ ;else
      ifelse View = "mNeptune"
      [
        ask turtles
        [
          ifelse (fluorescence / 120 > marker-detection-threashold)
          [
            set color scale-color red fluorescence 0 255
          ]
          [
            set color black
          ]
        ] ;end ask
      ]
      [ ; else
        if View = "dead cells"
        [
          ask turtles
          [
            ifelse state = "dead"
            [
              set color lime
            ]
            [
              set color black
            ]
          ]
        ] ;endif
      ] ;endif
    ] ;endif
  ] ; endif
end

to output-views
  set View "mNeptune"
  update-colors
  export-view (word "mNeptune-" behaviorspace-run-number "-" ticks ".png")

  set View "dead cells"
  update-colors
  export-view (word "dead-" behaviorspace-run-number "-" ticks ".png")

  set View "chromatin"
  update-colors
  export-view (word "chromatin-" behaviorspace-run-number "-" ticks ".png")
end

;;**********************
;; Funciones de turtles:
;;**********************

to init-infected-cell
  set state "infected"
  ;set color [0 0 0]
  set infected-time 0
  set chromatin-condensed false
end

to init-healthy-cell
  set state "healthy"
  ;set color 87
  set infected-time 0
  set chromatin-condensed false
end

;basado en http://mathworld.wolfram.com/DiskPointPicking.html
to t-select-position
  let r (random-float 1) * max-pxcor ^ 2
  let theta random-float 360
  let x sqrt(r) * cos(theta)
  let y sqrt(r) * sin(theta)
  setxy x y
end

to-report t-decide-condense-chromatin
  ;report random-float 1 < initial-probability-of-chromatin-condensation
  report random-float 100.00 < (1 - (1 - initial-probability-of-chromatin-condensation / 100) ^ infected-time) * 100.00
end

to-report t-decide-infection
  report random-float 100.00 < infection-rate
end

to-report t-decide-glow
  report random-float 100.00 < mNeptune-effectiveness
end

to-report t-decide-die
  ;report random-float 100.00 * (1 - infected-time / (total-time * death-factor / 100)) < initial-probability-of-death
  report random-float 100.00 < (1 - (1 - initial-probability-of-death / 100) ^ infected-time) * 100.00
end

to t-condense-chromatin
  if (state = "infected") and (not chromatin-condensed) and t-decide-condense-chromatin
  [
    set chromatin-condensed true
  ]
end

to t-infect
  ask turtles in-radius viral-reach
    [
      if (not (self = myself)) and (state = "healthy") and (t-decide-infection) ;lanza un dado para ver si infecta a cada vecino
      [
        set state "infected" ;si sí, el vecino se infecta
        ;set color [0 0 0]
      ]
    ]
end

to t-glow
  if-else mNeptune-effectiveness > 100
  [
    set fluorescence fluorescence + mNeptune-effectiveness / 100
  ]
  [
    if t-decide-glow ;lanza un dado para ver si aumenta su brillo
    [
      set fluorescence fluorescence + 1 ;si sí, aumenta en 1
    ]
  ]
end

to t-die
  if t-decide-die
  [
    set state "dead"
  ]
end

to-report t-report-reachable-neighbors
  report count turtles in-radius viral-reach
end

;;**********************
;; Funciones de patches:
;;**********************

to init-patch ;; Para inicializar una parcela a la vez.
  set pcolor black
end

;;********************
;; Funciones de links:
;;********************

to init-link ;; Para inicializar un link o conexión a la vez.

end

to l-comportamiento-link ;; Cambiar por nombre significativo de comportamiento de link

end

;;*********************
;; Funciones de breeds:
;;*********************
@#$#@#$#@
GRAPHICS-WINDOW
342
10
854
523
-1
-1
2.51
1
10
1
1
1
0
0
0
1
-100
100
-100
100
1
1
1
ticks
2.0

SLIDER
7
251
246
284
initial-infected-cell-percentage
initial-infected-cell-percentage
0.0
100.0
0.0
0.001
1
%
HORIZONTAL

BUTTON
8
84
100
117
setup-random
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
139
10
243
48
go-once
go
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
0

BUTTON
139
53
243
91
go-forever
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
0

MONITOR
176
145
336
190
proportion of infected cells
count turtles with [state = \"infected\" or state = \"dead\"] / count turtles
2
1
11

BUTTON
8
47
93
80
NIL
setup-blank
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
8
10
63
43
clear
clear-all
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
5
332
138
365
infection-rate
infection-rate
0
15
15.0
0.1
1
%
HORIZONTAL

SLIDER
6
372
215
405
mNeptune-effectiveness
mNeptune-effectiveness
0
400
100.0
0.01
1
%
HORIZONTAL

SLIDER
5
412
220
445
initial-probability-of-death
initial-probability-of-death
0
2
0.6
0.001
1
%
HORIZONTAL

INPUTBOX
264
10
337
70
total-time
120.0
1
0
Number

PLOT
864
10
1237
203
Cells
time
number of cells
0.0
120.0
0.0
1100.0
true
true
"" ""
PENS
"dead cells" 1.0 0 -13840069 true "" "plot count turtles with [state = \"dead\"]"
"infected (mNeptune)" 1.0 0 -2674135 true "" "plot count turtles with [state = \"infected\" and fluorescence / 120 > marker-detection-threashold]"
"cells with condensed chromatin" 1.0 0 -13791810 true "" "plot count turtles with [chromatin-condensed]"
"dead and condensed" 1.0 0 -8630108 true "" "plot count turtles with [state = \"dead\" and chromatin-condensed]"
"alive (mNeptune) and condensed" 1.0 0 -1184463 true "" "plot count turtles with [state = \"infected\" and fluorescence / 120 > marker-detection-threashold and chromatin-condensed]"

SLIDER
5
489
220
522
marker-detection-threashold
marker-detection-threashold
0
1
0.12
0.01
1
NIL
HORIZONTAL

BUTTON
8
159
101
192
update-colors
update-colors
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

SLIDER
7
211
179
244
cell-density
cell-density
0
20
2.0
0.01
1
NIL
HORIZONTAL

PLOT
864
369
1237
522
Reachable neighbors
Number of neighbors
Cells
0.0
50.0
0.0
4000.0
true
false
"" ""
PENS
"default" 1.0 0 -16777216 true "" ""

SLIDER
6
291
139
324
viral-reach
viral-reach
0
3
1.5
0.1
1
NIL
HORIZONTAL

SLIDER
5
451
314
484
initial-probability-of-chromatin-condensation
initial-probability-of-chromatin-condensation
0
2.5
2.5
0.05
1
%
HORIZONTAL

CHOOSER
197
196
335
241
View
View
"chromatin" "mNeptune" "dead cells"
0

PLOT
864
210
1237
362
Velocity
time
velocity of growth
0.0
120.0
-5.0
30.0
true
false
"" ""
PENS
"dead cells" 1.0 0 -13840069 true "" ""
"alive (mNeptune)" 1.0 0 -2674135 true "" ""
"cells with condensed chromatin" 1.0 0 -13791810 true "" ""
"dead and condensed" 1.0 0 -8630108 true "" ""
"alive (mNeptune) and condensed" 1.0 0 -1184463 true "" ""

MONITOR
207
95
336
140
NIL
initially-infected-cells
17
1
11

BUTTON
8
122
124
155
NIL
setup-experiment
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

@#$#@#$#@
## ¿DE QUÉ SE TRATA?

(una descripción general de lo que el modelo trata de modelar o explicar)

## ¿CÓMO FUNCIONA?

(qué reglas usan los agentes para orginar el funcionamiento del modelo)

## ¿CÓMO USARLO?

(cómo usar el modelo, incluye una descripción de cada uno de los controles en la interfaz)

## ¿QUÉ TOMAR EN CUENTA?

(cosas que debe tener en cuenta el usuario al ejecutar el modelo)

## ¿QUÉ PROBAR?

(sugerencias para el usuario sobre qué pruebas realizar (mover los "sliders", los "switches", etc.) con el modelo)

## EXTENDIENDO EL MODELO

(sugerencias sobre cómo realizar adiciones o cambios en el código del modelo para hacerlo más complejo, detallado, preciso, etc.)

## CARACTERÍSTICAS NETLOGO

(características interesantes o inusuales de NetLogo que usa el modelo, particularmente de código; o cómo se logra implementar características inexistentes)

## MODELOS RELACIONADOS 

(otros modelos de interés disponibles en la Librería de Modelos de NetLogo o en otros repositorios de modelos)

## CRÉDITOS AND REFERENCIAS

(referencia a un URL en Internet si es que el modelo tiene una, así como los créditos necesarios, citas y otros hipervínculos)

## ODD - ESPECIFICACIÓN DETALLADA DEL MODELO

## Título
(nombre del modelo)

## Autores
(nombre de los autores del modelo)

## Visión
## 1  Objetivos:
( 1.1  )
## 2  Entidades, variables de estado y escalas:
( 2.1 ) 
## 3  Visión del proceso y programación:
( 3.1  )

## Conceptos del diseño
## 4  Propiedades del modelo:
##  4.1  Básicas:
()
##  4.2  Emergentes:
()
##  4.3  Adaptabilidad:
()
##  4.4  Metas:
()
##  4.5  Aprendizaje:
()
##  4.6  Predictibilidad:
()
##  4.7  Sensibilidad:
()
##  4.8  Interacciones:
()
##  4.9  Estocasticidad:
()
##  4.10  Colectividades:
()
##  4.11  Salidas:
()
## Detalles
##  5  Inicialización:
()
##  6  Datos de entrada:
()
##  7  Submodelos:
()
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

sheep
false
0
Rectangle -7500403 true true 151 225 180 285
Rectangle -7500403 true true 47 225 75 285
Rectangle -7500403 true true 15 75 210 225
Circle -7500403 true true 135 75 150
Circle -16777216 true false 165 76 116

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

wolf
false
0
Polygon -7500403 true true 135 285 195 285 270 90 30 90 105 285
Polygon -7500403 true true 270 90 225 15 180 90
Polygon -7500403 true true 30 90 75 15 120 90
Circle -1 true false 183 138 24
Circle -1 true false 93 138 24

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270
@#$#@#$#@
NetLogo 6.0.3
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
<experiments>
  <experiment name="experiment" repetitions="10" runMetricsEveryStep="false">
    <setup>setup-experiment</setup>
    <go>go</go>
    <final>export-all-plots (word "experiment-" behaviorspace-run-number ".csv")</final>
    <metric>count turtles with [state = "dead"]</metric>
    <metric>count turtles with [state = "infected"]</metric>
    <metric>count turtles with [chromatin-condensed]</metric>
    <metric>count turtles with [state = "dead" and chromatin-condensed]</metric>
    <metric>count turtles with [state = "infected" and chromatin-condensed]</metric>
    <steppedValueSet variable="viral-reach" first="1.9" step="0.1" last="2.1"/>
    <steppedValueSet variable="initial-probability-of-death" first="0.1" step="0.1" last="0.5"/>
    <steppedValueSet variable="infection-rate" first="0.5" step="0.5" last="1.5"/>
    <enumeratedValueSet variable="initial-infected-cell-percentage">
      <value value="0.006"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="marker-detection-threashold">
      <value value="0"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="mNeptune-effectiveness">
      <value value="100"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="total-time">
      <value value="120"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="cell-density">
      <value value="1.6"/>
    </enumeratedValueSet>
    <enumeratedValueSet variable="View">
      <value value="&quot;mNeptune&quot;"/>
    </enumeratedValueSet>
    <steppedValueSet variable="initial-probability-of-chromatin-condensation" first="0.1" step="0.1" last="2"/>
  </experiment>
</experiments>
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180
@#$#@#$#@
0
@#$#@#$#@
