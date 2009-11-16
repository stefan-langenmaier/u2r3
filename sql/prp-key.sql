SELECT pa1.subject, pa2.subject, hk.id, ca1.id, pa1.id, ca2.id, pa2.id
FROM hasKey AS hk
	INNER JOIN (
		SELECT DISTINCT MIN(name) as name, anz
		FROM (
			SELECT pax.id, l.name, anzl.anz, pax.subject, pax.property, pax.object
			FROM list AS l
				INNER JOIN (
					SELECT name, COUNT(name) AS anz
					FROM list
				) AS anzl
					ON anzl.name = l.name
				INNER JOIN (
					SELECT id, subject, property, object
					FROM objectPropertyAssertion
					UNION
					SELECT id, subject, property, object
					FROM dataPropertyAssertion
				) AS pax
					ON l.element = pax.property
				INNER JOIN (
					SELECT id, subject, property, object
					FROM objectPropertyAssertion
					UNION
					SELECT id, subject, property, object
					FROM dataPropertyAssertion
				) AS pay
					ON l.element = pay.property AND pax.property = pay.property AND pax.object = pay.object
			)
		GROUP BY subject
		HAVING COUNT(subject) = 2*anz
	) AS thel
		ON thel.name = hk.list
	INNER JOIN classAssertionEnt AS ca1
		ON ca1.class = hk.class
	INNER JOIN (
		SELECT pa.id, l.name, anzl.anz, pa.subject, pa.property, pa.object
		FROM list AS l
			INNER JOIN (
				SELECT name, COUNT(name) AS anz
				FROM list
			) AS anzl
				ON anzl.name = l.name
			INNER JOIN (
				SELECT id, subject, property, object
				FROM objectPropertyAssertion
				UNION
				SELECT id, subject, property, object
				FROM dataPropertyAssertion
			) AS pa
				ON l.element = pa.property
	) AS pa1
		ON ca1.entity = pa1.subject AND pa1.name = thel.name
	INNER JOIN classAssertionEnt AS ca2
		ON ca2.class = hk.class
	INNER JOIN (
		SELECT pa.id, l.name, anzl.anz, pa.subject, pa.property, pa.object
		FROM list AS l
			INNER JOIN (
				SELECT name, COUNT(name) AS anz
				FROM list
			) AS anzl
				ON anzl.name = l.name
			INNER JOIN (
				SELECT id, subject, property, object
				FROM objectPropertyAssertion
				UNION
				SELECT id, subject, property, object
				FROM dataPropertyAssertion
			) AS pa
				ON l.element = pa.property
	) AS pa2
		ON ca2.entity = pa2.subject AND pa2.name = thel.name AND pa1.object = pa2.object