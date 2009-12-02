SELECT id
FROM subClass AS t1
WHERE EXISTS
	(SELECT class
	FROM intersectionOf AS t2
	WHERE t2.class = t1.sub
		AND ( EXISTS
				(SELECT element
				FROM list AS t8
				WHERE t8.name = t2.list
				AND EXISTS
					(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#A'
					WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#A' = t8.element)
			)
			AND EXISTS
				(SELECT element
				FROM list AS t9
				WHERE t9.name = t2.list
				AND EXISTS
					(SELECT part
					FROM someValuesFrom AS t4
					WHERE t9.element = t4.part
						AND EXISTS
							(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#r'
							WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#r' = t4.property)
						AND EXISTS
							(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#C'
							WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#C' = t4.total)
					)
				)
		)
		
	)
	AND EXISTS
	(SELECT class
	FROM intersectionOf AS t5
	WHERE t5.class = t1.super
		AND ( EXISTS
				(SELECT element
				FROM list AS t10
				WHERE t10.name = t5.list
				AND EXISTS
					(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#B'
					WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#B' = t10.element)
				)
			OR EXISTS
				(SELECT element
				FROM list AS t11
				WHERE t11.name = t5.list
				AND EXISTS
					(SELECT part
					FROM allValuesFrom AS t7
					WHERE t11.element = t7.part
						AND EXISTS
							(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#p'
							WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#p' = t7.property)
						AND EXISTS
							(SELECT 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#D'
							WHERE 'http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#D' = t7.total)
					)
				)
			)
	)