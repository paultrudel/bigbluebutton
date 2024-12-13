import styled, { css, keyframes } from 'styled-components';
import {
  colorPrimary,
  listItemBgHover,
  itemFocusBorder,
  colorGray,
  colorWhite,
  colorOffWhite,
  userListBg,
  colorSuccess,
  colorDanger,
  colorGrayLighter,
  colorGrayLightest,
  colorText,

} from '/imports/ui/stylesheets/styled-components/palette';
import {
  borderSize,
  mdPaddingY,
  userIndicatorsOffset,
  indicatorPadding,
  xsPadding,
  smPaddingY,
  borderRadius,
  smPaddingX,
} from '/imports/ui/stylesheets/styled-components/general';
import {
  fontSizeBase,
  fontSizeSmall, fontSizeSmaller, textFontWeight, titlesFontWeight,
} from '/imports/ui/stylesheets/styled-components/typography';
import { smallOnly } from '/imports/ui/stylesheets/styled-components/breakpoints';
import { FormControlLabel, Switch } from '@mui/material';
import { styled as materialStyled } from '@mui/material/styles';
import TextareaAutosize from 'react-autosize-textarea';
import Button from '@mui/material/Button';

type ListItemProps = {
  animations: boolean;
};

type PanelProps = {
  isChrome: boolean;
};

type AvatarProps = {
  color: string;
  avatar: string;
  key: string;
  moderator: boolean;
  animations?: boolean;
  presenter?: boolean;
  whiteboardAccess?: boolean;
  voice?: boolean;
  muted?: boolean;
  listenOnly?: boolean;
  noVoice?: boolean;
  talking?: boolean;
  emoji?: string;
  isChrome?: boolean;
  isFirefox?: boolean;
  isEdge?: boolean;
};

const ListItem = styled.div<ListItemProps>`
  display: flex;
  flex-flow: row;
  flex-direction: row;
  align-items: center;
  border-radius: 5px;

  ${({ animations }) => animations && `
    transition: all .3s;
  `}

  &:first-child {
    margin-top: 0;
  }

  &:focus {
    background-color: ${listItemBgHover};
    box-shadow: inset 0 0 0 ${borderSize} ${itemFocusBorder}, inset 1px 0 0 1px ${itemFocusBorder};
    outline: none;
  }

  flex-shrink: 0;
`;

const UserContentContainer = styled.div`
  display: flex;
  flex: 1;
  overflow: hidden;
  align-items: center;
  flex-direction: row;
  gap: 0.5rem;
`;

const UserAvatarContainer = styled.div`
  min-width: 2.25rem;
`;

const UserName = styled.div`
  min-width: 0;
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: initial;
  color: ${colorGray};
  font-weight: ${textFontWeight};
  line-height: 120%;
`;

const PendingUsers = styled.div`
  display: flex;
  flex-direction: column;
`;

const MainTitle = styled.div`
  color: ${colorGray};
  font-weight: ${textFontWeight};
  flex: 1 0 0;
`;

const UsersWrapper = styled.div`
  display: flex;
  flex-direction: column;
`;

const Users = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const Panel = styled.div<PanelProps>`
  background-color: ${colorWhite};
  display: flex;
  flex-grow: 1;
  flex-direction: column;
  justify-content: flex-start;
  overflow: hidden;
  gap: 1rem;

  ${({ isChrome }) => isChrome && `
    transform: translateZ(0);
  `}

  @media ${smallOnly} {
    transform: none !important;
  }
`;

const pulse = (color: string) => keyframes`
    0% {
      box-shadow: 0 0 0 0 ${color}80;
    }
    100% {
      box-shadow: 0 0 0 10px ${color}00;
    }
  }
`;

const Avatar = styled.div<AvatarProps>`
  position: relative;
  height: 2rem;
  width: 2rem;
  border-radius: 50%;
  text-align: center;
  font-size: .85rem;
  border: 2px solid transparent;
  user-select: none;
  ${
  ({ color }) => css`
    background-color: ${color};
  `}
  }

  ${({ animations }) => animations && `
    transition: .3s ease-in-out;
  `}

  &:after,
  &:before {
    content: "";
    position: absolute;
    width: 0;
    height: 0;
    padding-top: .5rem;
    padding-right: 0;
    padding-left: 0;
    padding-bottom: 0;
    color: inherit;
    top: auto;
    left: auto;
    bottom: ${userIndicatorsOffset};
    right: ${userIndicatorsOffset};
    border: 1.5px solid ${userListBg};
    border-radius: 50%;
    background-color: ${colorSuccess};
    color: ${colorWhite};
    opacity: 0;
    font-family: 'bbb-icons';
    font-size: .65rem;
    line-height: 0;
    text-align: center;
    vertical-align: middle;
    letter-spacing: -.65rem;
    z-index: 1;

    [dir="rtl"] & {
      left: ${userIndicatorsOffset};
      right: auto;
      padding-right: .65rem;
      padding-left: 0;
    }

    ${({ animations }) => animations && `
      transition: .3s ease-in-out;
    `}
  }

  ${({ moderator }) => moderator && `
    border-radius: 5px;
  `}

  ${({ presenter }) => presenter && `
    &:before {
      content: "\\00a0\\e90b\\00a0";
      padding: ${mdPaddingY} !important;
      opacity: 1;
      top: ${userIndicatorsOffset};
      left: ${userIndicatorsOffset};
      bottom: auto;
      right: auto;
      border-radius: 5px;
      background-color: ${colorPrimary};

      [dir="rtl"] & {
        left: auto;
        right: ${userIndicatorsOffset};
        letter-spacing: -.33rem;
      }
    }
  `}

  ${({
    presenter, isChrome, isFirefox, isEdge,
  }) => presenter && (isChrome || isFirefox || isEdge) && `
    &:before {
      padding: ${indicatorPadding} !important;
    }
  `}

  ${({ whiteboardAccess, presenter }) => whiteboardAccess && !presenter && `
    &:before {
      content: "\\00a0\\e925\\00a0";
      padding: ${mdPaddingY} !important;
      border-radius: 50% !important;
      opacity: 1;
      top: ${userIndicatorsOffset};
      left: ${userIndicatorsOffset};
      bottom: auto;
      right: auto;
      border-radius: 5px;
      background-color: ${colorPrimary};

      [dir="rtl"] & {
        left: auto;
        right: ${userIndicatorsOffset};
        letter-spacing: -.33rem;
        transform: scale(-1, 1);
      }
    }
  `}

  ${({
    whiteboardAccess, isChrome, isFirefox, isEdge,
  }) => whiteboardAccess && (isChrome || isFirefox || isEdge) && `
    &:before {
      padding: ${indicatorPadding};
    }
  `}

  ${({ voice }) => voice && `
    &:after {
      content: "\\00a0\\e931\\00a0";
      background-color: ${colorSuccess};
      top: 1.375rem;
      left: 1.375rem;
      right: auto;

      [dir="rtl"] & {
        left: auto;
        right: 1.375rem;
      }
      opacity: 1;
      width: 1.2rem;
      height: 1.2rem;
    }
  `}

  ${({ muted }) => muted && `
    &:after {
      content: "\\00a0\\e932\\00a0";
      background-color: ${colorDanger};
      opacity: 1;
      width: 1.2rem;
      height: 1.2rem;
    }
  `}

  ${({ listenOnly }) => listenOnly && `
    &:after {
      content: "\\00a0\\e90c\\00a0";
      opacity: 1;
      width: 1.2rem;
      height: 1.2rem;
    }
  `}

  ${({ noVoice }) => noVoice && `
    &:after {
      content: "";
      background-color: ${colorOffWhite};
      top: 1.375rem;
      left: 1.375rem;
      right: auto;

      [dir="rtl"] & {
        left: auto;
        right: 1.375rem;
      }

      opacity: 1;
      width: 1.2rem;
      height: 1.2rem;
    }
  `}

  // ================ talking animation ================
  ${({ talking, animations, color }) => talking && animations && css`
    animation: ${pulse(color)} 1s infinite ease-in;
  `}
  // ================ talking animation ================
  // ================ image ================
  ${({ avatar, emoji }) => avatar.length !== 0 && !emoji && css`
    background-image: url(${avatar});
    background-repeat: no-repeat;
    background-size: contain;
  `}
  // ================ image ================

  // ================ content ================
  color: ${colorWhite};
  font-size: 110%;
  text-transform: capitalize;
  display: flex;
  justify-content: center;
  align-items:center;  
  // ================ content ================
`;

const WaitingUsersHeader = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  align-self: stretch; 
`;

const WaitingUsersContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  align-self: stretch;
`;

const GuestNumberIndicator = styled.div`
  color: ${colorWhite};
  font-size: ${fontSizeSmaller};
  font-style: normal;
  font-weight: ${titlesFontWeight};
  line-height: normal;
`;

const GuestOptionsContainer = styled.div`
  flex-shrink: 0;
  display: flex;
  background: #F4F6FA;
  padding: 0.25rem 0.5rem;
  align-items: center;
  border-radius: 1.5rem;
  height: 1.5rem;
`;

const AcceptDenyButtonsContainer = styled.div`
  display: inline-flex;
  justify-content: flex-end;
  align-items: center;
  gap: 1.5rem;
`;

const AcceptDenyButtonText = styled.div`
  font-size: ${fontSizeSmall};
  font-weight: ${textFontWeight};
  line-height: 120%;
  text-decoration-line: underline;
  text-decoration-style: solid;
  text-decoration-skip-ink: none;
  text-decoration-thickness: auto;
  text-underline-offset: auto;
  text-underline-position: from-font;
`;

const AcceptAllButton = styled.div`
  display: flex;
  color: ${colorPrimary};
  align-items: center;
  gap: 0.5rem;
  font-size: ${fontSizeSmall};
  cursor: pointer;
`;

const DenyAllButton = styled.div`
  display: flex;
  color: ${colorDanger};
  align-items: center;
  gap: 0.5rem;
  font-size: ${fontSizeSmall};
  cursor: pointer;
`;

const GuestLobbyMessageContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  align-self: stretch;
  width: 100%;
  gap: 0.5rem;
  padding-top: 1rem;
`;

const SwitchTitle = styled(FormControlLabel)`
  //height: 1.5rem;
  //width: 1.5rem;
  //flex-shrink: 0;
  .MuiFormControlLabel-label {
    color: ${colorText};
    font-size: ${fontSizeBase}
    font-weight: ${textFontWeight};
    line-height: normal;
  }
`;

const MessageSwitch = materialStyled(Switch)(({ theme }) => ({
  width: 22,
  height: 12,
  padding: 0,
  display: 'flex',
  '&:active': {
    '& .MuiSwitch-thumb': {
      // width: 10,
    },
    '& .MuiSwitch-switchBase.Mui-checked': {
      transform: 'translateX(9px)',
    },
  },
  '& .MuiSwitch-switchBase': {
    padding: 2,
    '&.Mui-checked': {
      transform: 'translateX(12px)',
      color: '#fff',
      '& + .MuiSwitch-track': {
        opacity: 1,
        backgroundColor: colorPrimary,
        ...theme.applyStyles('dark', {
          backgroundColor: colorPrimary,
        }),
      },
    },
  },
  '& .MuiSwitch-thumb': {
    boxShadow: '0 2px 4px 0 rgb(0 35 11 / 20%)',
    width: 6,
    height: 6,
    borderRadius: 6,
    transition: theme.transitions.create(['width'], {
      duration: 200,
    }),
    transform: 'translateY(1px)',
  },
  '& .MuiSwitch-track': {
    borderRadius: 16 / 2,
    opacity: 1,
    backgroundColor: 'rgba(0,0,0,.25)',
    boxSizing: 'border-box',
    ...theme.applyStyles('dark', {
      backgroundColor: 'rgba(255,255,255,.35)',
    }),
  },
}));

const SendButton = styled(Button)`
  align-self: center;
  font-size: 0.9rem;
  height: 100%;

  & > span {
    height: 100%;
    display: flex;
    align-items: center;
    border-radius: 0 0.75rem 0.75rem 0;
  }

  [dir="rtl"]  & {
    -webkit-transform: scale(-1, 1);
    -moz-transform: scale(-1, 1);
    -ms-transform: scale(-1, 1);
    -o-transform: scale(-1, 1);
    transform: scale(-1, 1);
  }
`;

const Input = styled(TextareaAutosize)`
  flex: 1;
  background: #fff;
  background-clip: padding-box;
  margin: ${xsPadding} 0 ${xsPadding} ${xsPadding};
  color: ${colorGrayLighter};
  -webkit-appearance: none;
  padding: calc(${smPaddingY} * 2.5) 0 calc(${smPaddingX} * 1.25) calc(${smPaddingY} * 2.5);
  resize: none;
  transition: color 0.3s ease;
  border-radius: ${borderRadius};
  font-size: ${fontSizeBase};
  line-height: 1;
  min-height: 2.5rem;
  max-height: 3.5rem;
  overflow-y: auto;
  box-shadow: none;
  outline: none;

  border: 1px solid ${colorGrayLightest};

  [dir='ltr'] & {
    border-radius: 0.75rem 0 0 0.75rem;
  }

  [dir='rtl'] & {
    border-radius: 0 0.75rem 0.75rem 0;
  }

  &:focus {
    color: ${colorText};
  }

  &:disabled,
  &[disabled] {
    cursor: not-allowed;
    opacity: .75;
    background-color: rgba(167,179,189,0.25);
  }
`;

const InputWrapper = styled.div`
  display: flex;
  flex-direction: row;
  flex-grow: 1;
  min-width: 0;
  width: 100%;
  z-index: 0;
  border-radius: 0.75rem;
  height: 3.5rem;
`;

const NoMessageText = styled.div`
  padding-left: 2.5rem;
  color: ${colorText};
  font-size: ${fontSizeSmall};
`;

const GuestLobbyMessage = styled.div`
  color: ${colorText};
  font-size: ${fontSizeSmall};
  font-style: italic;  
`;

export default {
  ListItem,
  UserContentContainer,
  UserAvatarContainer,
  UserName,
  PendingUsers,
  MainTitle,
  UsersWrapper,
  Users,
  Panel,
  Avatar,
  WaitingUsersHeader,
  GuestNumberIndicator,
  GuestOptionsContainer,
  AcceptDenyButtonsContainer,
  AcceptAllButton,
  DenyAllButton,
  AcceptDenyButtonText,
  GuestLobbyMessageContainer,
  SwitchTitle,
  MessageSwitch,
  SendButton,
  Input,
  InputWrapper,
  NoMessageText,
  GuestLobbyMessage,
  WaitingUsersContainer,
};
