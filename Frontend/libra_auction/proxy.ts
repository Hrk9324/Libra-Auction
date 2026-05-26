import { NextResponse } from 'next/server'
import { NextRequest } from 'next/server'
import { isAuthenticated } from './lib/is_authenticated';
import { getRoles } from './lib/get_roles';

function hasRole(roles: Array<{ name: string }>, roleName: string) {
  return roles.some((role) => role.name === roleName);
}

export async function proxy(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  if (pathname == '/sign-in' || pathname == '/sign-up') {
    if (await isAuthenticated()) {
      return NextResponse.redirect(new URL('/', request.url))
    }
  }

  const isPersonalRoute = pathname.startsWith('/profile') || pathname.startsWith('/seller-dashboard');
  const isAdminRoute = pathname.startsWith('/admin-dashboard');

  if (isPersonalRoute || isAdminRoute) {
    const roles = await getRoles();

    if (roles.length === 0) {
      return NextResponse.redirect(new URL('/sign-in', request.url));
    }

    if (isAdminRoute && !hasRole(roles, 'ADMIN')) {
      return NextResponse.redirect(new URL('/', request.url));
    }

    if (isPersonalRoute && !hasRole(roles, 'USER')) {
      return NextResponse.redirect(new URL('/', request.url));
    }
  }
  
  return NextResponse.next();
}

export const config = {
  matcher: '/((?!api/auth|_next/static|_next_image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
}