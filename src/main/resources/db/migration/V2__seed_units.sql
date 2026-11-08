-- Common units. Admins can add more through POST /api/units.
-- Box / pack are mainly purchase units; conversions to stock units arrive in V3.
INSERT INTO unit (name, symbol) VALUES
    ('Kilogram',   'kg'),
    ('Gram',       'g'),
    ('Liter',      'l'),
    ('Milliliter', 'ml'),
    ('Piece',      'pc'),
    ('Box',        'box'),
    ('Pack',       'pack');
