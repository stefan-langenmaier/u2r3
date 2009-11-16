SELECT start.start, pc.property, ende.ende, ref.opaid AS sourceId1, 'objectPropertyAssertion' as sourceTable1, pc.id AS sourceId2, 'propertyChain' AS sourceTable2
FROM (
	SELECT lname, anz FROM (
		SELECT vopa.subject as vorgaenger, opa.subject AS start, opa.object AS ende, nopa.object as nachfolger, l.name AS lname, anzl.anz, opa.id AS opaid
		FROM list AS l
			INNER JOIN objectPropertyAssertion AS opa
				ON opa.property = l.element
			INNER JOIN (
				SELECT name, COUNT(name) AS anz
				FROM list
			) AS anzl ON anzl.name = l.name
			LEFT OUTER JOIN objectPropertyAssertion AS vopa
				ON vopa.object = opa.subject
			LEFT OUTER JOIN objectPropertyAssertion AS nopa
				ON nopa.subject = opa.object
			WHERE EXISTS (
				 SELECT 1
				 FROM list AS sl
				 	INNER JOIN objectPropertyAssertion AS sopa ON sl.element = sopa.property
				 WHERE l.name = sl.name AND (opa.object = sopa.subject OR opa.subject = sopa.object)
			)
		)
		GROUP BY lname
		HAVING COUNT(lname) = anz
	) AS thel
	INNER JOIN (
		SELECT lname, start
		FROM (
			SELECT vopa.subject as vorgaenger, opa.subject AS start, opa.object AS ende, nopa.object as nachfolger, l.name AS lname, anzl.anz, opa.id AS opaid
			FROM list AS l
				INNER JOIN objectPropertyAssertion AS opa
					ON opa.property = l.element
				INNER JOIN (
					SELECT name, COUNT(name) AS anz
					FROM list
				) AS anzl ON anzl.name = l.name
				LEFT OUTER JOIN objectPropertyAssertion AS vopa
					ON vopa.object = opa.subject
				LEFT OUTER JOIN objectPropertyAssertion AS nopa
					ON nopa.subject = opa.object
				WHERE EXISTS (
					 SELECT 1
					 FROM list AS sl
					 	INNER JOIN objectPropertyAssertion AS sopa ON sl.element = sopa.property
					 WHERE l.name = sl.name AND (opa.object = sopa.subject OR opa.subject = sopa.object)
				)
			)
		WHERE vorgaenger IS NULL
	) AS start
		ON start.lname = thel.lname
	INNER JOIN (
		SELECT lname, ende
		FROM (
			SELECT vopa.subject as vorgaenger, opa.subject AS start, opa.object AS ende, nopa.object as nachfolger, l.name AS lname, anzl.anz, opa.id AS opaid
			FROM list AS l
				INNER JOIN objectPropertyAssertion AS opa
					ON opa.property = l.element
				INNER JOIN (
					SELECT name, COUNT(name) AS anz
					FROM list
				) AS anzl ON anzl.name = l.name
				LEFT OUTER JOIN objectPropertyAssertion AS vopa
					ON vopa.object = opa.subject
				LEFT OUTER JOIN objectPropertyAssertion AS nopa
					ON nopa.subject = opa.object
				WHERE EXISTS (
					 SELECT 1
					 FROM list AS sl
					 	INNER JOIN objectPropertyAssertion AS sopa ON sl.element = sopa.property
					 WHERE l.name = sl.name AND (opa.object = sopa.subject OR opa.subject = sopa.object)
				)
			)
		WHERE nachfolger IS NULL
	) AS ende
		ON ende.lname = thel.lname
	INNER JOIN (
		SELECT lname, opaid
		FROM (
			SELECT vopa.subject as vorgaenger, opa.subject AS start, opa.object AS ende, nopa.object as nachfolger, l.name AS lname, anzl.anz, opa.id AS opaid
			FROM list AS l
				INNER JOIN objectPropertyAssertion AS opa
					ON opa.property = l.element
				INNER JOIN (
					SELECT name, COUNT(name) AS anz
					FROM list
				) AS anzl ON anzl.name = l.name
				LEFT OUTER JOIN objectPropertyAssertion AS vopa
					ON vopa.object = opa.subject
				LEFT OUTER JOIN objectPropertyAssertion AS nopa
					ON nopa.subject = opa.object
				WHERE EXISTS (
					 SELECT 1
					 FROM list AS sl
					 	INNER JOIN objectPropertyAssertion AS sopa ON sl.element = sopa.property
					 WHERE l.name = sl.name AND (opa.object = sopa.subject OR opa.subject = sopa.object)
				)
			)
	) AS ref
		ON ref.lname = thel.lname
	INNER JOIN propertyChain AS pc
		ON pc.list = thel.lname