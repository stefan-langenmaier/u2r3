SELECT ca1.entity AS left, ca2.entity AS right,
	hk.id AS sourceId1, 'hasKey' AS sourceTable1,
	ca1.id AS sourceId2, 'classAssertionEnt' AS sourceTable2,
	pa1.id AS sourceId3, pa1.type AS sourceTable1,
	ca2.id AS sourceId4, 'classAssertionEnt' AS sourceTable4,
	pa2.id AS sourceId5, pa2.type AS sourceTable5
FROM hasKey AS hk
	INNER JOIN list AS l
		ON l.name = hk.list
	INNER JOIN classAssertionEnt AS ca1
		ON ca1.class = hk.class
	INNER JOIN (
		SELECT id, subject, property, object, 'objectPropertyAssertion' AS type
		FROM objectPropertyAssertion
		UNION
		SELECT id, subject, property, object, 'dataPropertyAssertion' AS type
		FROM dataPropertyAssertion
	) AS pa1
		ON pa1.subject = ca1.entity AND pa1.property = l.element
	INNER JOIN classAssertionEnt AS ca2
		ON ca2.class = hk.class
	INNER JOIN (
		SELECT id, subject, property, object, 'objectPropertyAssertion' AS type
		FROM objectPropertyAssertion
		UNION
		SELECT id, subject, property, object, 'dataPropertyAssertion' AS type
		FROM dataPropertyAssertion
	) AS pa2
		ON pa2.subject = ca2.entity AND pa2.property = l.element AND pa1.object = pa2.object
	INNER JOIN (
		SELECT pax.subject, l.name, anzl.anz
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
		GROUP BY pax.subject
		HAVING COUNT(l.name) = 2*anz
	) AS valid1
		ON valid1.subject = ca1.entity
	INNER JOIN (
		SELECT pax.subject, l.name, anzl.anz
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
		GROUP BY pax.subject
		HAVING COUNT(l.name) = 2*anz
	) AS valid2
		ON valid2.subject = ca2.entity