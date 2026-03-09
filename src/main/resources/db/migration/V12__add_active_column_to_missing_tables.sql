-- Robust migration to add missing columns from BaseEntity
-- image_analyses already handled in V5

DO $$ 
BEGIN 
    -- Fix referrals: Add active if table exists but column doesn't
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'referrals') THEN
        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'referrals' AND column_name = 'active') THEN
            ALTER TABLE referrals ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;
    END IF;

    -- Fix access_grants: Add active if table exists but column doesn't
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'access_grants') THEN
        IF NOT EXISTS (SELECT FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'access_grants' AND column_name = 'active') THEN
            ALTER TABLE access_grants ADD COLUMN active BOOLEAN DEFAULT true;
        END IF;
    END IF;
END $$;
